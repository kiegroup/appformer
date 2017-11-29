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

import static org.guvnor.rest.backend.PermissionConstants.REST_PROJECT_ROLE;
import static org.guvnor.rest.backend.PermissionConstants.REST_ROLE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.io.IOService;
import org.uberfire.spaces.SpacesAPI;

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
    private WorkspaceProjectService workspaceProjectService;
    @Inject
    private SpacesAPI spaces;
    @Inject
    private JobRequestScheduler jobRequestObserver;
    @Inject
    private JobResultManager jobManager;
    private AtomicLong counter = new AtomicLong(0);

    private void addAcceptedJobResult(String jobId) {
        JobResult jobResult = new JobResult();
        jobResult.setJobId(jobId);
        jobResult.setStatus(JobStatus.ACCEPTED);
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

        String id = newId();
        CloneRepositoryRequest jobRequest = new CloneRepositoryRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setRepository(repository);
        addAcceptedJobResult(id);
        return createAcceptedStatusResponse(jobRequest);
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

        return createAcceptedStatusResponse(jobRequest);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/organizationalunits/{organizationalUnitName}/projects")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Collection<ProjectResponse> getProjects(@PathParam("organizationalUnitName") String organizationalUnitName) {
        logger.info("-----getProjects--- , organizationalUnitName: {}",
                    organizationalUnitName);

        return Collections.emptyList();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/projects")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Collection<ProjectResponse> getProjects() {
        logger.info("-----getProjects--- ");

        return Collections.emptyList();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/projects/{spaceName}/{projectName}")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response deleteProject(
            @PathParam("spaceName") String spaceName,
            @PathParam("projectName") String projectName) {
        logger.debug("-----deleteProject--- , project name: {}",
                     projectName);

        String id = newId();
        DeleteProjectRequest jobRequest = new DeleteProjectRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setProjectName(projectName);
        jobRequest.setSpaceName(spaceName);
        addAcceptedJobResult(id);

        return createAcceptedStatusResponse(jobRequest);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/projects/{spaceName}/{projectName}")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public ProjectResponse getProject(@PathParam("spaceName") String spaceName, @PathParam("projectName") String projectName) {
        logger.debug("-----getProject---, project name: {}",
                     projectName);

        final ProjectResponse projectResponse = new ProjectResponse();

        return projectResponse;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/projects/{spaceName}/{projectName}/maven/compile")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response compileProject(
            @PathParam("spaceName") String spaceName,
            @PathParam("projectName") String projectName) {
        logger.debug("-----compileProject--- , project name: {}",
                     projectName);

        String id = newId();
        CompileProjectRequest jobRequest = new CompileProjectRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setProjectName(projectName);
        jobRequest.setSpaceName(spaceName);
        addAcceptedJobResult(id);

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

        return createAcceptedStatusResponse(jobRequest);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/projects/{spaceName}/{projectName}/maven/test")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response testProject(
            @PathParam("spaceName") String spaceName,
            @PathParam("projectName") String projectName) {
        logger.debug("-----testProject--- , project name: {}",
                     projectName);

        String id = newId();
        TestProjectRequest jobRequest = new TestProjectRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setProjectName(projectName);
        jobRequest.setSpaceName(spaceName);
        addAcceptedJobResult(id);

        return createAcceptedStatusResponse(jobRequest);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/projects/{spaceName}/{projectName}/maven/deploy")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response deployProject(
            @PathParam("spaceName") String spaceName,
            @PathParam("projectName") String projectName) {
        logger.debug("-----deployProject--- , project name: {}",
                     projectName);

        String id = newId();
        DeployProjectRequest jobRequest = new DeployProjectRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setProjectName(projectName);
        jobRequest.setSpaceName(spaceName);
        addAcceptedJobResult(id);

        return createAcceptedStatusResponse(jobRequest);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/organizationalunits")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Collection<OrganizationalUnit> getOrganizationalUnits() {
        logger.debug("-----getOrganizationalUnits--- ");

        return Collections.emptyList();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/organizationalunits/{organizationalUnitName}")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public OrganizationalUnit getOrganizationalUnit(@PathParam("organizationalUnitName") String organizationalUnitName) {
        logger.debug("-----getOrganizationalUnit ---, OrganizationalUnit name: {}",
                     organizationalUnitName);

        return null;
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

        String id = newId();
        UpdateOrganizationalUnitRequest jobRequest = new UpdateOrganizationalUnitRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setOrganizationalUnitName(organizationalUnit.getName());
        jobRequest.setOwner(organizationalUnit.getOwner());
        jobRequest.setDefaultGroupId(organizationalUnit.getDefaultGroupId());
        addAcceptedJobResult(id);

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

        String id = newId();
        AddProjectToOrganizationalUnitRequest jobRequest = new AddProjectToOrganizationalUnitRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setOrganizationalUnitName(organizationalUnitName);
        jobRequest.setProjectName(projectName);
        addAcceptedJobResult(id);

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

        String id = newId();
        RemoveProjectFromOrganizationalUnitRequest jobRequest = new RemoveProjectFromOrganizationalUnitRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setOrganizationalUnitName(organizationalUnitName);
        jobRequest.setProjectName(projectName);
        addAcceptedJobResult(id);

        return createAcceptedStatusResponse(jobRequest);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/organizationalunits/{organizationalUnitName}")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response deleteOrganizationalUnit(@PathParam("organizationalUnitName") String organizationalUnitName) {
        logger.debug("-----deleteOrganizationalUnit--- , OrganizationalUnit name: {}",
                     organizationalUnitName);

        String id = newId();
        RemoveOrganizationalUnitRequest jobRequest = new RemoveOrganizationalUnitRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setOrganizationalUnitName(organizationalUnitName);
        addAcceptedJobResult(id);

        return createAcceptedStatusResponse(jobRequest);
    }

    private Response createAcceptedStatusResponse(JobRequest jobRequest) {
        return Response.status(Status.ACCEPTED).entity(jobRequest).variant(defaultVariant).build();
    }

    private String newId() {
        return "" + System.currentTimeMillis() + "-" + counter.incrementAndGet();
    }
}
