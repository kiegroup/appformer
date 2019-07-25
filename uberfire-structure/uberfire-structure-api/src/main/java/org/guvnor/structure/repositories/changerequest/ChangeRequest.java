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

import java.util.Date;
import java.util.Objects;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

import static org.kie.soup.commons.validation.PortablePreconditions.checkNotEmpty;
import static org.kie.soup.commons.validation.PortablePreconditions.checkNotNull;

@Portable
public class ChangeRequest {

    private Long id;
    private String spaceName;
    private String repositoryAlias;
    private String sourceBranch;
    private String targetBranch;
    private ChangeRequestStatus status;
    private String authorId;
    private String summary;
    private String description;
    private Date createdDate;
    private Integer changedFilesCount;
    private Integer commentsCount;
    private String commonCommitId;
    private String lastCommitId;
    private Boolean conflict;

    public ChangeRequest(final long id,
                         final String spaceName,
                         final String repositoryAlias,
                         final String sourceBranch,
                         final String targetBranch,
                         final ChangeRequestStatus status,
                         final String authorId,
                         final String summary,
                         final String description,
                         final Date createdDate,
                         final String commonCommitId) {
        this(id,
             spaceName,
             repositoryAlias,
             sourceBranch,
             targetBranch,
             status,
             authorId,
             summary,
             description,
             createdDate,
             commonCommitId,
             null);
    }

    public ChangeRequest(final long id,
                         final String spaceName,
                         final String repositoryAlias,
                         final String sourceBranch,
                         final String targetBranch,
                         final ChangeRequestStatus status,
                         final String authorId,
                         final String summary,
                         final String description,
                         final Date createdDate,
                         final String commonCommitId,
                         final String lastCommitId) {
        this(id,
             spaceName,
             repositoryAlias,
             sourceBranch,
             targetBranch,
             status,
             authorId,
             summary,
             description,
             createdDate,
             0,
             0,
             commonCommitId,
             lastCommitId,
             false);
    }

    public ChangeRequest(@MapsTo("id") final Long id,
                         @MapsTo("spaceName") final String spaceName,
                         @MapsTo("repositoryAlias") final String repositoryAlias,
                         @MapsTo("sourceBranch") final String sourceBranch,
                         @MapsTo("targetBranch") final String targetBranch,
                         @MapsTo("status") final ChangeRequestStatus status,
                         @MapsTo("authorId") final String authorId,
                         @MapsTo("summary") final String summary,
                         @MapsTo("description") final String description,
                         @MapsTo("createdDate") final Date createdDate,
                         @MapsTo("changedFilesCount") final Integer changedFilesCount,
                         @MapsTo("commentsCount") final Integer commentsCount,
                         @MapsTo("commonCommitId") final String commonCommitId,
                         @MapsTo("lastCommitId") final String lastCommitId,
                         @MapsTo("conflict") final Boolean conflict) {

        this.id = checkNotNull("id",
                               id);
        this.spaceName = checkNotEmpty("spaceName",
                                       spaceName);
        this.repositoryAlias = checkNotEmpty("repositoryAlias",
                                             repositoryAlias);
        this.sourceBranch = checkNotEmpty("sourceBranch",
                                          sourceBranch);
        this.targetBranch = checkNotEmpty("targetBranch",
                                          targetBranch);
        this.status = checkNotNull("status",
                                   status);
        this.authorId = checkNotEmpty("authorId",
                                      authorId);
        this.summary = checkNotEmpty("summary",
                                     summary);
        this.description = checkNotEmpty("description",
                                         description);
        this.createdDate = checkNotNull("createdDate",
                                        createdDate);
        this.changedFilesCount = checkNotNull("changedFilesCount",
                                              changedFilesCount);
        this.commentsCount = checkNotNull("commentsCount",
                                          commentsCount);
        this.commonCommitId = checkNotEmpty("commonCommitId",
                                            commonCommitId);
        this.lastCommitId = lastCommitId; // can be null
        this.conflict = checkNotNull("conflict",
                                     conflict);
    }

    public long getId() {
        return this.id;
    }

    public String getSpaceName() {
        return this.spaceName;
    }

    public String getRepositoryAlias() {
        return this.repositoryAlias;
    }

    public String getSourceBranch() {
        return this.sourceBranch;
    }

    public String getTargetBranch() {
        return this.targetBranch;
    }

    public ChangeRequestStatus getStatus() {
        return this.status;
    }

    public String getAuthorId() {
        return this.authorId;
    }

    public String getSummary() {
        return this.summary;
    }

    public String getDescription() {
        return this.description;
    }

    public Date getCreatedDate() {
        return this.createdDate;
    }

    public Integer getChangedFilesCount() {
        return this.changedFilesCount;
    }

    public Integer getCommentsCount() {
        return this.commentsCount;
    }

    public String getCommonCommitId() {
        return commonCommitId;
    }

    public String getLastCommitId() {
        return lastCommitId;
    }

    public Boolean isConflict() {
        return conflict;
    }

    @Override
    public String toString() {
        return "#" + this.id + " " + this.summary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChangeRequest that = (ChangeRequest) o;
        return id.equals(that.id) &&
                spaceName.equals(that.spaceName) &&
                repositoryAlias.equals(that.repositoryAlias) &&
                sourceBranch.equals(that.sourceBranch) &&
                targetBranch.equals(that.targetBranch) &&
                status == that.status &&
                authorId.equals(that.authorId) &&
                summary.equals(that.summary) &&
                description.equals(that.description) &&
                createdDate.equals(that.createdDate) &&
                changedFilesCount.equals(that.changedFilesCount) &&
                commentsCount.equals(that.commentsCount) &&
                commonCommitId.equals(that.commonCommitId) &&
                lastCommitId.equals(that.lastCommitId) &&
                conflict.equals(that.conflict);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,
                            spaceName,
                            repositoryAlias,
                            sourceBranch,
                            targetBranch,
                            status,
                            authorId,
                            summary,
                            description,
                            createdDate,
                            changedFilesCount,
                            commentsCount,
                            commonCommitId,
                            lastCommitId,
                            conflict);
    }
}
