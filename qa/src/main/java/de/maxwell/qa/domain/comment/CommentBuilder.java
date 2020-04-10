/*
 * MIT License
 *
 * Copyright (c) 2020 Max Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.maxwell.qa.domain.comment;

import java.time.LocalDateTime;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

public class CommentBuilder {

    private Comment comment;

    public CommentBuilder() {
        this.comment = new Comment();
    }

    public CommentBuilder withUserID(final String userID) {
        notNull(userID, "userId cannot be null");
        this.comment.setUserID(userID);
        return this;
    }

    public CommentBuilder withQuestionID(final Long questionID) {
        this.comment.setQuestionID(questionID);
        return this;
    }

    public CommentBuilder withAnswerID(final Long answerID) {
        this.comment.setAnswerID(answerID);
        return this;
    }

    public CommentBuilder withRating(final Long rating) {
        notNull(rating, "rating cannot be null");
        this.comment.setRating(rating);
        return this;
    }

    public CommentBuilder withDescription(final String description) {
        notNull(description, "description cannot be null");
        notEmpty(description, "description cannot be empty");
        this.comment.setDescription(description);
        return this;
    }

    public CommentBuilder withCreatedAt(final LocalDateTime createdAt) {
        notNull(createdAt, "createdAt cannot be null");
        this.comment.setCreatedAt(createdAt);
        return this;
    }

    public CommentBuilder withModifiedAt(final LocalDateTime modifiedAt) {
        notNull(modifiedAt, "modifiedAt cannot be null");
        this.comment.setModifiedAt(modifiedAt);
        return this;
    }

    public Comment build() throws IllegalStateException {
        if (this.comment.getQuestionID() != null && this.comment.getAnswerID() != null) {
            throw new IllegalStateException("QuestionID and AnswerID cannot be set at once");
        } else if (this.comment.getQuestionID() == null && this.comment.getAnswerID() == null) {
            throw new IllegalStateException("QuestionID or AnswerID has to be set");
        }
        return this.comment;
    }

}
