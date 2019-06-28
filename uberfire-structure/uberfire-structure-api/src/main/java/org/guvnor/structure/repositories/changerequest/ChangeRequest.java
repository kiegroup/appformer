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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

import static org.kie.soup.commons.validation.PortablePreconditions.checkNotEmpty;
import static org.kie.soup.commons.validation.PortablePreconditions.checkNotNull;

@Portable
public class ChangeRequest {

    private long id;
    private String spaceName;
    private String repositoryAlias;
    private String sourceBranch;
    private String targetBranch;
    private ChangeRequestStatus status;
    private String author;
    private String summary;
    private String description;
    private Date createdDate;
    private Integer changedFilesCount;
    private List<ChangeRequestComment> comments;

    public ChangeRequest() {

    }

    public ChangeRequest(final String spaceName,
                         final String repositoryAlias,
                         final String sourceBranch,
                         final String targetBranch,
                         final String author,
                         final String summary,
                         final String description,
                         final Date createdDate,
                         final Integer changedFilesCount,
                         final List<ChangeRequestComment> comments) {
        this(0L,
             spaceName,
             repositoryAlias,
             sourceBranch,
             targetBranch,
             ChangeRequestStatus.OPEN,
             author,
             summary,
             description,
             createdDate,
             changedFilesCount,
             comments);
    }

    public ChangeRequest(final long id,
                         final String spaceName,
                         final String repositoryAlias,
                         final String sourceBranch,
                         final String targetBranch,
                         final String author,
                         final String summary,
                         final String description,
                         final Integer changedFilesCount) {
        this(id,
             spaceName,
             repositoryAlias,
             sourceBranch,
             targetBranch,
             ChangeRequestStatus.OPEN,
             author,
             summary,
             description,
             new Date(),
             changedFilesCount,
             new ArrayList<>());
    }

    public ChangeRequest(@MapsTo("id") final long id,
                         @MapsTo("spaceName") final String spaceName,
                         @MapsTo("repositoryAlias") final String repositoryAlias,
                         @MapsTo("sourceBranch") final String sourceBranch,
                         @MapsTo("targetBranch") final String targetBranch,
                         @MapsTo("status") final ChangeRequestStatus status,
                         @MapsTo("author") final String author,
                         @MapsTo("summary") final String summary,
                         @MapsTo("description") final String description,
                         @MapsTo("createdDate") final Date createdDate,
                         @MapsTo("changedFilesCount") final Integer changedFilesCount,
                         @MapsTo("comments") final List<ChangeRequestComment> comments) {

        this.id = id;
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
        this.author = checkNotEmpty("author",
                                    author);
        this.summary = checkNotEmpty("summary",
                                     summary);
        this.description = checkNotEmpty("description",
                                         description);
        this.createdDate = checkNotNull("createdDate",
                                        createdDate);
        this.changedFilesCount = checkNotNull("changedFilesCount",
                                              changedFilesCount);
        this.comments = checkNotNull("comments",
                                     comments);
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

    public String getAuthor() {
        return this.author;
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

    public List<ChangeRequestComment> getComments() {
        return this.comments;
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
        return id == that.id &&
                spaceName.equals(that.spaceName) &&
                repositoryAlias.equals(that.repositoryAlias) &&
                sourceBranch.equals(that.sourceBranch) &&
                targetBranch.equals(that.targetBranch) &&
                status == that.status &&
                author.equals(that.author) &&
                summary.equals(that.summary) &&
                description.equals(that.description) &&
                createdDate.equals(that.createdDate) &&
                changedFilesCount.equals(that.changedFilesCount) &&
                comments.equals(that.comments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, spaceName, repositoryAlias, sourceBranch, targetBranch, status, author, summary, description, createdDate, changedFilesCount, comments);
    }
}
