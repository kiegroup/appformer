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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import org.guvnor.common.services.project.model.GAV;
import org.guvnor.common.services.project.model.WorkspaceProject;
import org.guvnor.common.services.project.service.WorkspaceProjectService;
import org.guvnor.rest.client.AddProjectToOrganizationalUnitRequest;
import org.guvnor.rest.client.CloneRepositoryRequest;
import org.guvnor.rest.client.CompileProjectRequest;
import org.guvnor.rest.client.CreateOrganizationalUnitRequest;
import org.guvnor.rest.client.CreateProjectRequest;
import org.guvnor.rest.client.DeleteProjectRequest;
import org.guvnor.rest.client.DeployProjectRequest;
import org.guvnor.rest.client.InstallProjectRequest;
import org.guvnor.rest.client.JobRequest;
import org.guvnor.rest.client.JobResult;
import org.guvnor.rest.client.JobStatus;
import org.guvnor.rest.client.OrganizationalUnit;
import org.guvnor.rest.client.ProjectRequest;
import org.guvnor.rest.client.ProjectResponse;
import org.guvnor.rest.client.RemoveOrganizationalUnitRequest;
import org.guvnor.rest.client.RemoveProjectFromOrganizationalUnitRequest;
import org.guvnor.rest.client.RepositoryRequest;
import org.guvnor.rest.client.TestProjectRequest;
import org.guvnor.rest.client.UpdateOrganizationalUnit;
import org.guvnor.rest.client.UpdateOrganizationalUnitRequest;
import org.guvnor.structure.organizationalunit.OrganizationalUnitService;
import org.guvnor.structure.repositories.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.io.IOService;

import static org.guvnor.rest.backend.PermissionConstants.REST_PROJECT_ROLE;
import static org.guvnor.rest.backend.PermissionConstants.REST_ROLE;

/**
 * REST services
 */
@Path("/")
@Named
@ApplicationScoped
public class ProjectResource {

    private static final Logger logger = LoggerFactory.getLogger(ProjectResource.class);
    private static Variant defaultVariant = Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE).add().build().get(0);
    @Context
    protected UriInfo uriInfo;
    @Inject
    @Named("ioStrategy")
    private IOService ioService;
    @Inject
    private OrganizationalUnitService organizationalUnitService;
    @Inject
    private RepositoryService repositoryService;
    @Inject
    private WorkspaceProjectService workspaceProjectService;
    @Inject
    private JobRequestScheduler jobRequestObserver;
    @Inject
    private JobResultManager jobManager;
    private AtomicLong counter = new AtomicLong(0);

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

        JobResult job = jobManager.removeJob(jobId);

        if (job == null) {
            //the job has gone probably because its done and has been removed.
            logger.debug("-----removeJob--- , can not find jobId: " + jobId + ", the job has gone probably because its done and has been removed.");
            job = new JobResult();
            job.setStatus(JobStatus.GONE);
            return job;
        }

        job.setStatus(JobStatus.GONE);
        return job;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/repositories")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response cloneRepository(RepositoryRequest repository) {
        logger.debug("-----cloneRepository--- , repository name: {}",
                     repository.getName());

        checkOrganizationalUnitExistence(repository.getOrganizationalUnitName());

        String id = newId();
        CloneRepositoryRequest jobRequest = new CloneRepositoryRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setRepository(repository);

        String reqType = repository.getRequestType();
        if (reqType == null || reqType.trim().isEmpty()
                || !("new".equals(reqType) || "clone".equals(reqType))) {
            jobRequest.setStatus(JobStatus.BAD_REQUEST);
            return Response.status(Status.BAD_REQUEST).entity(jobRequest).variant(defaultVariant).build();
        } else {
            addAcceptedJobResult(id);
            jobRequestObserver.cloneRepositoryRequest(jobRequest);
            return createAcceptedStatusResponse(jobRequest);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/organizationalunits/{organizationalUnitName}/projects")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response createProject(
            @PathParam("organizationalUnitName") String organizationalUnitName,
            ProjectRequest project) {
        logger.debug("-----createProject--- , organizationalUnitName: {} , project name: {}",
                     organizationalUnitName,
                     project.getName());

        String id = newId();
        CreateProjectRequest jobRequest = new CreateProjectRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setOrganizationalUnitName(organizationalUnitName);
        jobRequest.setProjectName(project.getName());
        jobRequest.setProjectGroupId(project.getGroupId());
        jobRequest.setProjectVersion(project.getVersion());
        jobRequest.setDescription(project.getDescription());

        addAcceptedJobResult(id);

        jobRequestObserver.createProjectRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/organizationalunits/{organizationalUnitName}/projects")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Collection<ProjectResponse> getProjects(@PathParam("organizationalUnitName") String organizationalUnitName) {
        logger.info("-----getProjects--- , organizationalUnitName: {}",
                    organizationalUnitName);

        final org.guvnor.structure.organizationalunit.OrganizationalUnit organizationalUnit = organizationalUnitService.getOrganizationalUnit(organizationalUnitName);
        if (organizationalUnit == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(organizationalUnitName).build());
        }

        return getProjectResponses(workspaceProjectService.getAllWorkspaceProjects(organizationalUnit));
    }

    private List<ProjectResponse> getProjectResponses(Collection<WorkspaceProject> workspaceProjects) {
        final List<ProjectResponse> projectRequests = new ArrayList<>(workspaceProjects.size());
        for (final WorkspaceProject workspaceProject : workspaceProjects) {
            final ProjectResponse projectReq = new ProjectResponse();
            final GAV projectGAV = workspaceProject.getMainModule().getPom().getGav();

            projectReq.setGroupId(projectGAV.getGroupId());
            projectReq.setVersion(projectGAV.getVersion());
            projectReq.setName(workspaceProject.getName());
            projectReq.setDescription(workspaceProject.getMainModule().getPom().getDescription());
            projectRequests.add(projectReq);
        }
        return projectRequests;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/projects")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Collection<ProjectResponse> getProjects() {
        logger.info("-----getProjects--- ");

        return getProjectResponses(workspaceProjectService.getAllWorkspaceProjects());
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/projects/{projectName}")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response deleteProject(
            @PathParam("projectName") String projectName) {
        logger.debug("-----deleteProject--- , project name: {}",
                     projectName);

        String id = newId();
        DeleteProjectRequest jobRequest = new DeleteProjectRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setProjectName(projectName);

        addAcceptedJobResult(id);

        jobRequestObserver.deleteProjectRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/projects/{projectName}")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public ProjectResponse getProject(@PathParam("projectName") String projectName) {
        logger.debug("-----getProject---, project name: {}",
                     projectName);

        final WorkspaceProject workspaceProject = workspaceProjectService.resolveProject(projectName);

        final ProjectResponse projectResponse = new ProjectResponse();
        final GAV projectGAV = workspaceProject.getMainModule().getPom().getGav();

        projectResponse.setGroupId(projectGAV.getGroupId());
        projectResponse.setVersion(projectGAV.getVersion());
        projectResponse.setName(workspaceProject.getName());
        projectResponse.setDescription(workspaceProject.getMainModule().getPom().getDescription());

        return projectResponse;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/projects/{projectName}/maven/compile")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response compileProject(
            @PathParam("projectName") String projectName) {
        logger.debug("-----compileProject--- , project name: {}",
                     projectName);

        String id = newId();
        CompileProjectRequest jobRequest = new CompileProjectRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setProjectName(projectName);

        addAcceptedJobResult(id);

        jobRequestObserver.compileProjectRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/organizationalunits/{organizationalUnitName}/projects/{projectName}/maven/install")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response installProject(
            @PathParam("organizationalUnitName") String organizationalUnitName,
            @PathParam("projectName") String projectName) {
        logger.debug("-----installProject--- , project name: {}",
                     projectName);

        String id = newId();
        InstallProjectRequest jobRequest = new InstallProjectRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setOrganizationalUnitName(organizationalUnitName);
        jobRequest.setProjectName(projectName);

        addAcceptedJobResult(id);

        jobRequestObserver.installProjectRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/projects/{projectName}/maven/test")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response testProject(
            @PathParam("projectName") String projectName) {
        logger.debug("-----testProject--- , project name: {}",
                     projectName);

        String id = newId();
        TestProjectRequest jobRequest = new TestProjectRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setProjectName(projectName);

        addAcceptedJobResult(id);

        jobRequestObserver.testProjectRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/projects/{projectName}/maven/deploy")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response deployProject(
            @PathParam("projectName") String projectName) {
        logger.debug("-----deployProject--- , project name: {}",
                     projectName);

        String id = newId();
        DeployProjectRequest jobRequest = new DeployProjectRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setProjectName(projectName);

        addAcceptedJobResult(id);

        jobRequestObserver.deployProjectRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/organizationalunits")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Collection<OrganizationalUnit> getOrganizationalUnits() {
        logger.debug("-----getOrganizationalUnits--- ");
        final Collection<org.guvnor.structure.organizationalunit.OrganizationalUnit> origOrgUnits
                = organizationalUnitService.getAllOrganizationalUnits();

        final List<OrganizationalUnit> organizationalUnits = new ArrayList<OrganizationalUnit>();
        for (final org.guvnor.structure.organizationalunit.OrganizationalUnit ou : origOrgUnits) {
            final OrganizationalUnit orgUnit = new OrganizationalUnit();
            orgUnit.setName(ou.getName());
            orgUnit.setOwner(ou.getOwner());
            orgUnit.setDefaultGroupId(ou.getDefaultGroupId());

            final List<String> projectNames = new ArrayList<String>();
            for (final WorkspaceProject workspaceProject : workspaceProjectService.getAllWorkspaceProjects(ou)) {
                projectNames.add(workspaceProject.getName());
            }

            orgUnit.setProjects(projectNames);
            organizationalUnits.add(orgUnit);
        }

        return organizationalUnits;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/organizationalunits/{organizationalUnitName}")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public OrganizationalUnit getOrganizationalUnit(@PathParam("organizationalUnitName") String organizationalUnitName) {
        logger.debug("-----getOrganizationalUnit ---, OrganizationalUnit name: {}",
                     organizationalUnitName);
        final org.guvnor.structure.organizationalunit.OrganizationalUnit origOrgUnit
                = checkOrganizationalUnitExistence(organizationalUnitName);

        final OrganizationalUnit orgUnit = new OrganizationalUnit();
        orgUnit.setName(origOrgUnit.getName());
        orgUnit.setOwner(origOrgUnit.getOwner());
        orgUnit.setDefaultGroupId(origOrgUnit.getDefaultGroupId());

        final List<String> projectNames = new ArrayList<>();
        for (final WorkspaceProject workspaceProject : workspaceProjectService.getAllWorkspaceProjects(origOrgUnit)) {
            projectNames.add(workspaceProject.getName());
        }

        orgUnit.setProjects(projectNames);

        return orgUnit;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/organizationalunits")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response createOrganizationalUnit(OrganizationalUnit organizationalUnit) {
        logger.debug("-----createOrganizationalUnit--- , OrganizationalUnit name: {}, OrganizationalUnit owner: {}, Default group id : {}",
                     organizationalUnit.getName(),
                     organizationalUnit.getOwner(),
                     organizationalUnit.getDefaultGroupId());

        String id = newId();
        CreateOrganizationalUnitRequest jobRequest = new CreateOrganizationalUnitRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setOrganizationalUnitName(organizationalUnit.getName());
        jobRequest.setOwner(organizationalUnit.getOwner());
        jobRequest.setDefaultGroupId(organizationalUnit.getDefaultGroupId());
        jobRequest.setProjects(organizationalUnit.getProjects());

        addAcceptedJobResult(id);

        jobRequestObserver.createOrganizationalUnitRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/organizationalunits/{organizationalUnitName}/")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response updateOrganizationalUnit(@PathParam("organizationalUnitName") String orgUnitName,
                                             UpdateOrganizationalUnit organizationalUnit) {

        // use name in url if post entity name is null
        if (organizationalUnit.getName() == null) {
            organizationalUnit.setName(orgUnitName);
        }

        logger.debug("-----updateOrganizationalUnit--- , OrganizationalUnit name: {}, OrganizationalUnit owner: {}, Default group id : {}",
                     organizationalUnit.getName(),
                     organizationalUnit.getOwner(),
                     organizationalUnit.getDefaultGroupId());

        org.guvnor.structure.organizationalunit.OrganizationalUnit origOrgUnit
                = checkOrganizationalUnitExistence(orgUnitName);

        // use owner in existing OU if post owner is null
        if (organizationalUnit.getOwner() == null) {
            organizationalUnit.setOwner(origOrgUnit.getOwner());
        }

        String id = newId();
        UpdateOrganizationalUnitRequest jobRequest = new UpdateOrganizationalUnitRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setOrganizationalUnitName(organizationalUnit.getName());
        jobRequest.setOwner(organizationalUnit.getOwner());
        jobRequest.setDefaultGroupId(organizationalUnit.getDefaultGroupId());

        addAcceptedJobResult(id);

        jobRequestObserver.updateOrganizationalUnitRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/organizationalunits/{organizationalUnitName}/projects/{projectName}")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response addRepositoryToOrganizationalUnit(@PathParam("organizationalUnitName") String organizationalUnitName,
                                                      @PathParam("projectName") String projectName) {
        logger.debug("-----addRepositoryToOrganizationalUnit--- , OrganizationalUnit name: {}, Project name: {}",
                     organizationalUnitName,
                     projectName);
        checkOrganizationalUnitExistence(organizationalUnitName);
        checkProjectExistence(projectName);

        String id = newId();
        AddProjectToOrganizationalUnitRequest jobRequest = new AddProjectToOrganizationalUnitRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setOrganizationalUnitName(organizationalUnitName);
        jobRequest.setProjectName(projectName);

        addAcceptedJobResult(id);

        jobRequestObserver.addProjectToOrganizationalUnitRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/organizationalunits/{organizationalUnitName}/projects/{projectName}")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response removeRepositoryFromOrganizationalUnit(@PathParam("organizationalUnitName") String organizationalUnitName,
                                                           @PathParam("projectName") String projectName) {
        logger.debug("-----removeRepositoryFromOrganizationalUnit--- , OrganizationalUnit name: {}, Repository name: {}",
                     organizationalUnitName,
                     projectName);
        checkOrganizationalUnitExistence(organizationalUnitName);
        checkProjectExistence(projectName);

        String id = newId();
        RemoveProjectFromOrganizationalUnitRequest jobRequest = new RemoveProjectFromOrganizationalUnitRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setOrganizationalUnitName(organizationalUnitName);
        jobRequest.setProjectName(projectName);

        addAcceptedJobResult(id);

        jobRequestObserver.removeProjectFromOrganizationalUnitRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/organizationalunits/{organizationalUnitName}")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response deleteOrganizationalUnit(@PathParam("organizationalUnitName") String organizationalUnitName) {
        logger.debug("-----deleteOrganizationalUnit--- , OrganizationalUnit name: {}",
                     organizationalUnitName);
        checkOrganizationalUnitExistence(organizationalUnitName);

        String id = newId();
        RemoveOrganizationalUnitRequest jobRequest = new RemoveOrganizationalUnitRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setOrganizationalUnitName(organizationalUnitName);

        addAcceptedJobResult(id);

        jobRequestObserver.removeOrganizationalUnitRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    private org.guvnor.structure.organizationalunit.OrganizationalUnit checkOrganizationalUnitExistence(String orgUnitName) {
        if (orgUnitName == null || orgUnitName.isEmpty()) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(orgUnitName).build());
        }

        org.guvnor.structure.organizationalunit.OrganizationalUnit origOrgUnit
                = organizationalUnitService.getOrganizationalUnit(orgUnitName);

        if (origOrgUnit == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(orgUnitName).build());
        }
        return origOrgUnit;
    }

    private WorkspaceProject checkProjectExistence(String projectName) {
        if (projectName == null || projectName.isEmpty()) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(projectName).build());
        }

        final WorkspaceProject workspaceProject = workspaceProjectService.resolveProject(projectName);

        if (workspaceProject == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(projectName).build());
        }
        return workspaceProject;
    }

    private Response createAcceptedStatusResponse(JobRequest jobRequest) {
        return Response.status(Status.ACCEPTED).entity(jobRequest).variant(defaultVariant).build();
    }

    private String newId() {
        return "" + System.currentTimeMillis() + "-" + counter.incrementAndGet();
    }
}
