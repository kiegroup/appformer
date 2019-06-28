/*
 * 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.guvnor.structure.repositories.changerequest;

import java.util.List;

import org.jboss.errai.bus.server.annotations.Remote;

/**
 * Service that contains the basic mechanism to administrate change requests.
 * Every change request depends on its repository.
 * The change request id is unique in every repository, but it can be repeated
 * across them.
 */
@Remote
public interface ChangeRequestService {

    /**
     * Creates a change request and stores it into the tracking system.
     * @param spaceName the space containing the origin repository
     * @param repositoryAlias the repository alias
     * @param sourceBranch the branch where you want to get pulled
     * @param targetBranch the branch where you want impact your changes
     * @param author the user id who submitted the change request
     * @param summary the short summary of the change request
     * @param description the description of the change request
     * @param changedFilesCount the number of changed files
     * @return The object that represents the change request.
     */
    ChangeRequest createChangeRequest(final String spaceName,
                                      final String repositoryAlias,
                                      final String sourceBranch,
                                      final String targetBranch,
                                      final String author,
                                      final String summary,
                                      final String description,
                                      final Integer changedFilesCount);

    /**
     * Retrieves the list of change requests that the user is able to visualize.
     * @param spaceName the space containing the origin repository
     * @param repositoryAlias the repository alias
     * @return The list of change requests.
     */
    List<ChangeRequest> getChangeRequests(final String spaceName,
                                          final String repositoryAlias);

    /**
     * Retrieves the list of change requests that the user is able to visualize.
     * @param spaceName the space containing the origin repository
     * @param repositoryAlias the repository alias
     * @param filter a string to filter the results
     * @return The list of change requests.
     */
    List<ChangeRequest> getChangeRequests(final String spaceName,
                                          final String repositoryAlias,
                                          final String filter);

    /**
     * Retrieves the list of change requests that the user is able to visualize.
     * @param spaceName the space containing the origin repository
     * @param repositoryAlias the repository alias
     * @param status change request status to filter the results
     * @return The list of change requests.
     */
    List<ChangeRequest> getChangeRequests(final String spaceName,
                                          final String repositoryAlias,
                                          final ChangeRequestStatus status);

    /**
     * Retrieves the list of change requests that the user is able to visualize.
     * @param spaceName the space containing the origin repository
     * @param repositoryAlias the repository alias
     * @param status change request status to filter the results
     * @param filter a string to filter the results
     * @return The list of change requests.
     */
    List<ChangeRequest> getChangeRequests(final String spaceName,
                                          final String repositoryAlias,
                                          final ChangeRequestStatus status,
                                          final String filter);

    /**
     * Retrieves the list of change requests that the user is able to visualize.
     * @param spaceName the space containing the origin repository
     * @param repositoryAlias the repository alias
     * @param page the desired page
     * @param pageSize the size of the page
     * @param filter a string to filter the results
     * @return The list of change requests.
     */
    List<ChangeRequest> getChangeRequests(final String spaceName,
                                          final String repositoryAlias,
                                          final Integer page,
                                          final Integer pageSize,
                                          final String filter);

    /**
     * Retrieves the list of change requests that the user is able to visualize.
     * @param spaceName the space containing the origin repository
     * @param repositoryAlias the repository alias
     * @param page the desired page
     * @param pageSize the size of the page
     * @param status change request status to filter the results
     * @param filter a string to filter the results
     * @return The list of change requests.
     */
    List<ChangeRequest> getChangeRequests(final String spaceName,
                                          final String repositoryAlias,
                                          final Integer page,
                                          final Integer pageSize,
                                          final ChangeRequestStatus status,
                                          final String filter);

    /**
     * Retrieves the change request with the given id.
     * @param spaceName the space containing the origin repository
     * @param repositoryAlias the repository used as a filter
     * @param id the id of the change request
     * @return The change request.
     */
    ChangeRequest getChangeRequest(final String spaceName,
                                   final String repositoryAlias,
                                   final Long id);

    /**
     * Retrieves the number of change requests that the user is able to visualize.
     * @param spaceName the space containing the origin repository
     * @param repositoryAlias the repository alias
     * @return The number of change requests.
     */
    Integer countChangeRequests(final String spaceName,
                                final String repositoryAlias);

    /**
     * Retrieves the number of change requests that the user is able to visualize.
     * @param spaceName the space containing the origin repository
     * @param repositoryAlias the repository alias
     * @param status change request status to filter the results
     * @return The number of change requests.
     */
    Integer countChangeRequests(final String spaceName,
                                final String repositoryAlias,
                                final ChangeRequestStatus status);

    /**
     * Retrieves the number of change requests that the user is able to visualize.
     * @param spaceName the space containing the origin repository
     * @param repositoryAlias the repository alias
     * @param filter a string to filter the results
     * @return The number of change requests.
     */
    Integer countChangeRequests(final String spaceName,
                                final String repositoryAlias,
                                final String filter);

    /**
     * Retrieves the number of change requests that the user is able to visualize.
     * @param spaceName the space containing the origin repository
     * @param repositoryAlias the repository alias
     * @param status change request status to filter the results
     * @param filter a string to filter the results
     * @return The number of change requests.
     */
    Integer countChangeRequests(final String spaceName,
                                final String repositoryAlias,
                                final ChangeRequestStatus status,
                                final String filter);

    /**
     * Obtains differences between branches.
     * @param spaceName the space containing the origin repository
     * @param repositoryAlias the origin repository
     * @param sourceBranchName the source branch
     * @param targetBranchName the target branch
     * @return The list of differences between files.
     */
    List<ChangeRequestDiff> getDiff(final String spaceName,
                                    final String repositoryAlias,
                                    final String sourceBranchName,
                                    final String targetBranchName);

    /**
     * Deletes all change requests associated with the given branch.
     * @param spaceName the space containing the origin repository
     * @param repositoryAlias the origin repository
     * @param associatedBranchName branch name
     */
    void deleteChangeRequests(final String spaceName,
                              final String repositoryAlias,
                              final String associatedBranchName);
}
