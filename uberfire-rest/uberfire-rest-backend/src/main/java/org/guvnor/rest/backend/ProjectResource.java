/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.guvnor.rest.backend;

import org.guvnor.common.services.project.model.WorkspaceProject;
import org.guvnor.common.services.project.service.WorkspaceProjectService;
import org.guvnor.rest.client.*;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.organizationalunit.OrganizationalUnitService;
import org.guvnor.structure.repositories.Branch;
import org.guvnor.structure.repositories.PublicURI;
import org.kie.soup.commons.validation.PortablePreconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.io.IOService;
import org.uberfire.rpc.SessionInfo;
import org.uberfire.spaces.SpacesAPI;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.guvnor.rest.backend.PermissionConstants.REST_PROJECT_ROLE;
import static org.guvnor.rest.backend.PermissionConstants.REST_ROLE;

/**
 * REST services for project management related operations
 */
@Path("/")
@Named
@ApplicationScoped
public class ProjectResource {

    private static final Logger logger = LoggerFactory.getLogger(ProjectResource.class);
    private static final String ACCEPT_LANGUAGE = "acceptLanguage";
    @Context
    protected UriInfo uriInfo;
    private Variant defaultVariant = getDefaultVariant();
    @Inject
    @Named("ioStrategy")
    private IOService ioService;
    @Inject
    private OrganizationalUnitService organizationalUnitService;
    @Inject
    private WorkspaceProjectService workspaceProjectService;
    @Inject
    private JobRequestScheduler jobRequestObserver;
    @Inject
    private JobResultManager jobManager;
    @Inject
    private SpacesAPI spacesAPI;
    @Inject
    private SessionInfo sessionInfo;
    private AtomicLong counter = new AtomicLong(0);

    protected Variant getDefaultVariant() {
        return Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE).add().build().get(0);
    }

    private void addAcceptedJobResult(String jobId) {
        JobResult jobResult = new JobResult();
        jobResult.setJobId(jobId);
        jobResult.setStatus(JobStatus.ACCEPTED);
        jobManager.putJob(jobResult);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/jobs/{jobId}")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public JobResult getJobStatus(@PathParam("jobId") String jobId) {
        logger.debug("-----getJobStatus--- , jobId: {}",
                jobId);

        JobResult job = jobManager.getJob(jobId);
        if (job == null) {
            //the job has gone probably because its done and has been removed.
            logger.debug("-----getJobStatus--- , can not find jobId: " + jobId + ", the job has gone probably because its done and has been removed.");
            job = new JobResult();
            job.setStatus(JobStatus.GONE);
            return job;
        }

        return job;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/jobs/{jobId}")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public JobResult removeJob(@PathParam("jobId") String jobId) {
        logger.debug("-----removeJob--- , jobId: {}",
                jobId);

        final JobResult job = getJobResult(jobId);
        job.setStatus(JobStatus.GONE);
        return job;
    }

    private JobResult getJobResult(String jobId) {
        final JobResult job = jobManager.removeJob(jobId);

        if (job == null) {
            //the job has gone probably because its done and has been removed.
            logger.debug("-----removeJob--- , can not find jobId: " + jobId + ", the job has gone probably because its done and has been removed.");
            return new JobResult();
        } else {
            return job;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spaces/{spaceName}/git/clone")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response cloneProject(@PathParam("spaceName") String spaceName,
                                 CloneProjectRequest cloneProjectRequest) {
        logger.debug("-----cloneProject--- , CloneProjectRequest name: {}",
                cloneProjectRequest.getName());

        final String id = newId();
        final CloneProjectJobRequest jobRequest = new CloneProjectJobRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setSpaceName(spaceName);
        jobRequest.setCloneProjectRequest(cloneProjectRequest);
        addAcceptedJobResult(id);

        jobRequestObserver.cloneProjectRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spaces/{spaceName}/projects")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response createProject(
            @PathParam("spaceName") String spaceName,
            @HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) Locale locales,
            CreateProjectRequest createProjectRequest) {
        logger.debug("-----createProject--- , spaceName: {} , project name: {}",
                spaceName,
                createProjectRequest.getName());

        assertObjectExists(organizationalUnitService.getOrganizationalUnit(spaceName),
                "space",
                spaceName);

        final Map<String, Object> headers = new HashMap<>();
        headers.put(ACCEPT_LANGUAGE, locales);
        final String id = newId();
        final CreateProjectJobRequest jobRequest = new CreateProjectJobRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setSpaceName(spaceName);
        jobRequest.setProjectName(createProjectRequest.getName());
        jobRequest.setProjectGroupId(createProjectRequest.getGroupId());
        jobRequest.setProjectVersion(createProjectRequest.getVersion());
        jobRequest.setDescription(createProjectRequest.getDescription());
        jobRequest.setTemplateId(createProjectRequest.getTemplateId());
        addAcceptedJobResult(id);

        jobRequestObserver.createProjectRequest(jobRequest,
                headers);

        return createAcceptedStatusResponse(jobRequest);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spaces/{spaceName}/projects")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Collection<ProjectResponse> getProjects(@PathParam("spaceName") String spaceName) {

        org.guvnor.structure.organizationalunit.OrganizationalUnit organizationalUnit = organizationalUnitService.getOrganizationalUnit(spaceName);

        assertObjectExists(organizationalUnit,
                "space",
                spaceName);

        logger.info("-----getProjects--- , spaceName: {}",
                organizationalUnit.getName());

        final Collection<WorkspaceProject> projects = workspaceProjectService.getAllWorkspaceProjects(organizationalUnit);

        final List<ProjectResponse> projectRequests = new ArrayList<ProjectResponse>(projects.size());
        for (WorkspaceProject project : projects) {
            projectRequests.add(getProjectResponse(project));
        }

        return projectRequests;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spaces/{spaceName}/projects/{projectName}")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response deleteProject(
            @PathParam("spaceName") String spaceName,
            @PathParam("projectName") String projectName) {

        OrganizationalUnit organizationalUnit = organizationalUnitService.getOrganizationalUnit(spaceName);
        final org.uberfire.spaces.Space space = spacesAPI.getSpace(spaceName);
        assertObjectExists(organizationalUnit,
                "space",
                spaceName);
        WorkspaceProject workspaceProject = workspaceProjectService.resolveProject(space, projectName);
        assertObjectExists(workspaceProject,
                "project",
                projectName);

        logger.debug("-----deleteProject--- , space name: {}, project name: {}",
                organizationalUnit.getName(),
                workspaceProject.getName());

        final String id = newId();
        final DeleteProjectRequest jobRequest = new DeleteProjectRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setProjectName(projectName);
        jobRequest.setSpaceName(spaceName);
        addAcceptedJobResult(id);

        jobRequestObserver.deleteProjectRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spaces/{spaceName}/projects/{projectName}")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public ProjectResponse getProject(@PathParam("spaceName") String spaceName,
                                      @PathParam("projectName") String projectName) {

        OrganizationalUnit organizationalUnit = organizationalUnitService.getOrganizationalUnit(spaceName);
        assertObjectExists(organizationalUnit,
                "space",
                spaceName);

        final WorkspaceProject workspaceProject = workspaceProjectService.resolveProject(spacesAPI.getSpace(spaceName), projectName);

        assertObjectExists(workspaceProject,
                "project",
                projectName);

        logger.debug("-----getProject---, space name: {}, project name: {}",
                organizationalUnit.getName(),
                workspaceProject.getName());

        final ProjectResponse projectResponse = getProjectResponse(workspaceProject);

        return projectResponse;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spaces/{spaceName}/projects/{projectName}/branches")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Collection<BranchResponse> getBranches(@PathParam("spaceName") String spaceName,
                                                  @PathParam("projectName") String projectName) {


        OrganizationalUnit organizationalUnit = organizationalUnitService.getOrganizationalUnit(spaceName);
        assertObjectExists(organizationalUnit,
                "space",
                spaceName);

        final WorkspaceProject workspaceProject = workspaceProjectService.resolveProject(
                spacesAPI.getSpace(spaceName),
                projectName);

        assertObjectExists(workspaceProject,
                "project",
                projectName);

        logger.debug("-----getBranches---, space name: {}, project name: {}",
                organizationalUnit.getName(),
                workspaceProject.getName());

        return workspaceProject
                .getRepository()
                .getBranches()
                .stream()
                .map(this::getBranchResponse)
                .collect(Collectors.toList());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spaces/{spaceName}/projects/{projectName}/branches")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response addBranch(@PathParam("spaceName") String spaceName,
                              @PathParam("projectName") String projectName,
                              AddBranchRequest addBranchRequest) {

        OrganizationalUnit organizationalUnit = organizationalUnitService.getOrganizationalUnit(spaceName);
        assertObjectExists(organizationalUnit,
                "space",
                spaceName);

        final WorkspaceProject workspaceProject = workspaceProjectService.resolveProject(
                spacesAPI.getSpace(spaceName),
                projectName);

        assertObjectExists(workspaceProject,
                "project",
                projectName);

        logger.debug("-----addBranch--- , spaceName: {} , project name: {}, branch Name: {}",
                organizationalUnit.getName(),
                workspaceProject.getName(),
                addBranchRequest.getNewBranchName());

        final String id = newId();
        final AddBranchJobRequest jobRequest = new AddBranchJobRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setSpaceName(spaceName);
        jobRequest.setProjectName(projectName);
        jobRequest.setNewBranchName(addBranchRequest.getNewBranchName());
        jobRequest.setBaseBranchName(addBranchRequest.getBaseBranchName());
        jobRequest.setUserIdentifier(sessionInfo.getIdentity().getIdentifier());
        addAcceptedJobResult(id);

        jobRequestObserver.addBranchRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spaces/{spaceName}/projects/{projectName}/branches/{branchName}")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response removeBranch(@PathParam("spaceName") String spaceName,
                                 @PathParam("projectName") String projectName,
                                 @PathParam("branchName") String branchName) {

        OrganizationalUnit organizationalUnit = organizationalUnitService.getOrganizationalUnit(spaceName);
        assertObjectExists(organizationalUnit,
                "space",
                spaceName);

        final WorkspaceProject workspaceProject = workspaceProjectService.resolveProject(
                spacesAPI.getSpace(spaceName),
                projectName,
                branchName);

        assertObjectExists(workspaceProject,
                "project",
                projectName);

        logger.debug("-----removeBranch--- , spaceName: {} , project name: {}, branch Name: {}",
                organizationalUnit.getName(),
                workspaceProject.getName(),
                workspaceProject.getBranch().getName());


        final String id = newId();
        final RemoveBranchJobRequest jobRequest = new RemoveBranchJobRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setSpaceName(spaceName);
        jobRequest.setProjectName(projectName);
        jobRequest.setBranchName(branchName);
        jobRequest.setUserIdentifier(sessionInfo.getIdentity().getIdentifier());
        addAcceptedJobResult(id);

        jobRequestObserver.removeBranchRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    private BranchResponse getBranchResponse(Branch branch) {
        final BranchResponse branchResponse = new BranchResponse();
        branchResponse.setName(branch.getName());
        return branchResponse;
    }

    private ProjectResponse getProjectResponse(WorkspaceProject workspaceProject) {
        final ProjectResponse projectResponse = new ProjectResponse();
        projectResponse.setName(workspaceProject.getName());
        projectResponse.setSpaceName(workspaceProject.getOrganizationalUnit().getName());

        if (workspaceProject.getMainModule() != null) {
            projectResponse.setGroupId(workspaceProject.getMainModule().getPom().getGav().getGroupId());
            projectResponse.setVersion(workspaceProject.getMainModule().getPom().getGav().getVersion());
            projectResponse.setDescription(workspaceProject.getMainModule().getPom().getDescription());
        }

        final ArrayList<org.guvnor.rest.client.PublicURI> publicURIs = new ArrayList<>();

        for (PublicURI publicURI : workspaceProject.getRepository().getPublicURIs()) {
            final org.guvnor.rest.client.PublicURI responseURI = new org.guvnor.rest.client.PublicURI();
            responseURI.setProtocol(publicURI.getProtocol());
            responseURI.setUri(publicURI.getURI());
            publicURIs.add(responseURI);
        }

        projectResponse.setPublicURIs(publicURIs);
        return projectResponse;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spaces/{spaceName}/projects/{projectName}/maven/compile")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response compileProject(
            @PathParam("spaceName") String spaceName,
            @PathParam("projectName") String projectName) {

        return compileProject(spaceName,
                projectName,
                null);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spaces/{spaceName}/projects/{projectName}/branches/{branchName}/maven/compile")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response compileProject(
            @PathParam("spaceName") String spaceName,
            @PathParam("projectName") String projectName,
            @PathParam("branchName") String branchName) {

        org.uberfire.spaces.Space space = spacesAPI.getSpace(spaceName);
        final WorkspaceProject workspaceProject = workspaceProjectService.resolveProject(
                spacesAPI.getSpace(spaceName),
                projectName,
                branchName);
        assertObjectExists(workspaceProject,
                "project",
                projectName);

        logger.debug("-----compileProject--- , space name: {}, project name: {}, branch name: {}",
                space.getName(),
                workspaceProject.getName(),
                workspaceProject.getBranch().getName());

        final String id = newId();
        final CompileProjectRequest jobRequest = new CompileProjectRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setProjectName(projectName);
        jobRequest.setSpaceName(spaceName);
        jobRequest.setBranchName(branchName);
        addAcceptedJobResult(id);

        jobRequestObserver.compileProjectRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spaces/{spaceName}/projects/{projectName}/maven/install")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response installProject(
            @PathParam("spaceName") String spaceName,
            @PathParam("projectName") String projectName) {

        return installProject(spaceName,
                projectName,
                null);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spaces/{spaceName}/projects/{projectName}/branches/{branchName}/maven/install")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response installProject(
            @PathParam("spaceName") String spaceName,
            @PathParam("projectName") String projectName,
            @PathParam("branchName") String branchName) {

        org.uberfire.spaces.Space space = spacesAPI.getSpace(spaceName);
        final WorkspaceProject workspaceProject = workspaceProjectService.resolveProject(
                space,
                projectName,
                branchName);
        assertObjectExists(workspaceProject,
                "project",
                projectName);
        logger.debug("-----installProject--- , project name: {}, branch name: {}",
                workspaceProject.getName(),
                workspaceProject.getBranch().getName());

        PortablePreconditions.checkNotNull("spaceName", spaceName);
        PortablePreconditions.checkNotNull("projectName", projectName);

        final String id = newId();
        final InstallProjectRequest jobRequest = new InstallProjectRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setSpaceName(spaceName);
        jobRequest.setProjectName(projectName);
        jobRequest.setBranchName(branchName);
        addAcceptedJobResult(id);

        jobRequestObserver.installProjectRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spaces/{spaceName}/projects/{projectName}/maven/test")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response testProject(
            @PathParam("spaceName") String spaceName,
            @PathParam("projectName") String projectName) {

        return testProject(spaceName,
                projectName,
                null);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spaces/{spaceName}/projects/{projectName}/branches/{branchName}/maven/test")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response testProject(
            @PathParam("spaceName") String spaceName,
            @PathParam("projectName") String projectName,
            @PathParam("branchName") String branchName) {

        org.uberfire.spaces.Space space = spacesAPI.getSpace(spaceName);
        final WorkspaceProject workspaceProject = workspaceProjectService.resolveProject(
                space,
                projectName,
                branchName);
        assertObjectExists(workspaceProject,
                "project",
                projectName);
        logger.debug("-----testProject--- , project name: {}, branch name: {}",
                workspaceProject.getName(),
                workspaceProject.getBranch().getName());

        final String id = newId();
        final TestProjectRequest jobRequest = new TestProjectRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setProjectName(projectName);
        jobRequest.setSpaceName(spaceName);
        jobRequest.setBranchName(branchName);
        addAcceptedJobResult(id);

        jobRequestObserver.testProjectRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spaces/{spaceName}/projects/{projectName}/maven/deploy")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response deployProject(
            @PathParam("spaceName") String spaceName,
            @PathParam("projectName") String projectName) {

        return deployProject(spaceName,
                projectName,
                null);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spaces/{spaceName}/projects/{projectName}/branches/{branchName}/maven/deploy")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response deployProject(
            @PathParam("spaceName") String spaceName,
            @PathParam("projectName") String projectName,
            @PathParam("branchName") String branchName) {

        org.uberfire.spaces.Space space = spacesAPI.getSpace(spaceName);
        final WorkspaceProject workspaceProject = workspaceProjectService.resolveProject(
                space,
                projectName,
                branchName);
        assertObjectExists(workspaceProject,
                "project",
                projectName);
        logger.debug("-----deployProject--- , project name: {}, branch name: {}",
                workspaceProject.getName(),
                workspaceProject.getBranch().getName());


        final String id = newId();
        final DeployProjectRequest jobRequest = new DeployProjectRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setProjectName(projectName);
        jobRequest.setSpaceName(spaceName);
        jobRequest.setBranchName(branchName);
        addAcceptedJobResult(id);

        jobRequestObserver.deployProjectRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spaces")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Collection<Space> getSpaces() {
        logger.debug("-----getSpaces--- ");

        final List<Space> spaces = new ArrayList<Space>();
        for (OrganizationalUnit ou : organizationalUnitService.getOrganizationalUnits()) {
            spaces.add(getSpace(ou));
        }

        return spaces;
    }

    private Space getSpace(OrganizationalUnit ou) {
        final Space space = new Space();
        space.setName(ou.getName());
        space.setDescription(ou.getDescription());
        space.setOwner(ou.getOwner());
        space.setDefaultGroupId(ou.getDefaultGroupId());

        final List<ProjectResponse> repoNames = new ArrayList<>();
        for (WorkspaceProject workspaceProject : workspaceProjectService.getAllWorkspaceProjects(ou)) {
            repoNames.add(getProjectResponse(workspaceProject));
        }

        space.setProjects(repoNames);
        return space;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spaces/{spaceName}")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Space getSpace(@PathParam("spaceName") String spaceName) {
        logger.debug("-----getSpace ---, Space name: {}",
                spaceName);
        final OrganizationalUnit ou = organizationalUnitService.getOrganizationalUnit(spaceName);

        assertObjectExists(ou,
                "space",
                spaceName);

        return getSpace(ou);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spaces")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response createSpace(Space space) {
        logger.debug("-----createSpace--- , Space name: {}, Space owner: {}, Default group id : {}",
                space.getName(),
                space.getOwner(),
                space.getDefaultGroupId());

        final String id = newId();
        final SpaceRequest jobRequest = new SpaceRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setSpaceName(space.getName());
        jobRequest.setDescription(space.getDescription());
        jobRequest.setOwner(space.getOwner());
        jobRequest.setDefaultGroupId(space.getDefaultGroupId());
        addAcceptedJobResult(id);

        jobRequestObserver.createSpaceRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spaces")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response updateSpace(Space space) {
        logger.debug("-----updateSpace--- , Space name: {}, Default group id : {}",
                space.getName(),
                space.getDefaultGroupId());

        final String id = newId();
        final SpaceRequest jobRequest = new SpaceRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setSpaceName(space.getName());
        jobRequest.setDescription(space.getDescription());
        jobRequest.setOwner(space.getOwner());
        jobRequest.setDefaultGroupId(space.getDefaultGroupId());
        addAcceptedJobResult(id);

        jobRequestObserver.updateSpaceRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spaces/{spaceName}")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response deleteSpace(@PathParam("spaceName") String spaceName) {
        OrganizationalUnit organizationalUnit = organizationalUnitService.getOrganizationalUnit(spaceName);
        assertObjectExists(organizationalUnit,
                "space",
                spaceName);

        logger.debug("-----deleteSpace--- , Space name: {}",
                organizationalUnit.getName());

        final String id = newId();
        final RemoveSpaceRequest jobRequest = new RemoveSpaceRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setSpaceName(spaceName);
        addAcceptedJobResult(id);

        jobRequestObserver.removeSpaceRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    protected void assertObjectExists(final Object o,
                                      final String objectInfo,
                                      final String objectName) {
        if (o == null) {
            throw new WebApplicationException(String.format("Could not find %s with name %s.", objectInfo, objectName),
                    Response.status(Status.NOT_FOUND).build());
        }
    }

    protected Response createAcceptedStatusResponse(final JobRequest jobRequest) {
        return Response.status(Status.ACCEPTED).entity(jobRequest).variant(defaultVariant).build();
    }

    private String newId() {
        return "" + System.currentTimeMillis() + "-" + counter.incrementAndGet();
    }
}
