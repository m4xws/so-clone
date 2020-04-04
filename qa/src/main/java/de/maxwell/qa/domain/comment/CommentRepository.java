package de.maxwell.qa.domain.comment;

import de.maxwell.qa.infrastructure.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

@Repository
public class CommentRepository {
    private static final Logger LOG = LoggerFactory.getLogger(CommentRepository.class);

    @Inject
    EntityManager em;

    /**
     * Find the comment by id
     *
     * @param id of the comment
     * @return comment
     */
    public Comment findById(final Long id) throws CommentNotFoundException {
        notNull(id, "id cannot be null");

        LOG.info("Find comment with id {}", id);

        Comment comment = em.find(Comment.class, id);
        if (comment == null) {
            LOG.info("Found no comment with id {}", id);
            throw new CommentNotFoundException(id);
        }

        return comment;
    }

    /**
     * Find paginated comments
     *
     * @param limit  max number of comment per page
     * @param offset of the page
     * @return list of comments
     */
    public List<Comment> listAllPaginated(final Integer limit, final Integer offset) {
        notNull(limit, "limit cannot be null");
        notNull(offset, "offset cannot be null");

        LOG.info("Find {} comments with offset {}", limit, offset);

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Comment> cq = cb.createQuery(Comment.class);

        Root<Comment> root = cq.from(Comment.class);
        cq.select(root);

        TypedQuery<Comment> query = em.createQuery(cq);
        query.setFirstResult(offset * limit);
        query.setMaxResults(limit);

        List<Comment> comments = query.getResultList();

        LOG.info("Found {} comments", comments.size());

        return comments;
    }

    /**
     * Create a new comment
     *
     * @param userID      user who asked the comment
     * @param description of the comment
     * @return
     */
    @Transactional
    public Comment createComment(final Long userID, final String description) throws IllegalArgumentException {
        try {
            notNull(userID, "userID cannot be null");
            notNull(description, "description cannot be null");
            notEmpty(description, "description cannot be empty");

            Comment comment = Comment.newBuilder()
                    .withUserID(userID)
                    .withDescription(description)
                    .build();

            em.persist(comment);

            LOG.info("Create comment with id {}", comment.getId());

            return comment;
        } catch (EntityExistsException e) {
            throw new IllegalArgumentException("Comment already exists");
        }
    }

    /**
     * update the description of the comment
     *
     * @param id             of the comment
     * @param newDescription
     * @return comment
     */
    @Transactional
    public Comment updateDescription(final Long id, final String newDescription) throws CommentNotFoundException {
        notNull(id, "id cannot be null");
        notNull(newDescription, "new description cannot be null");
        notEmpty(newDescription, "new description cannot be empty");

        Comment comment = em.find(Comment.class, id);
        if (comment == null) {
            LOG.info("Found no comment with id {}", id);
            throw new CommentNotFoundException(id);
        }

        LOG.info("Update description of comment with id {}", id);

        comment.setDescription(newDescription);
        comment.setModifiedAt(LocalDateTime.now());
        em.merge(comment);

        return comment;
    }

    /**
     * update the rating of the comment. Ratings can be negative!
     *
     * @param id     of the answer
     * @param rating new rating of the answer
     * @return new rating
     */
    @Transactional
    public Long updateRating(final Long id, final Integer rating) throws CommentNotFoundException, IllegalArgumentException {
        notNull(id, "id cannot be null");
        notNull(rating, "rating cannot be null");

        if (rating == 1 || rating == -1 || rating != 0) {
            Comment comment = em.find(Comment.class, id);
            if (comment == null) {
                LOG.info("Found no comment with id {}", id);
                throw new CommentNotFoundException(id);
            }

            LOG.info("Update rating of comment with id {}", id);

            comment.setRating(comment.getRating() + rating);
            comment.setModifiedAt(LocalDateTime.now());

            em.merge(comment);

            return comment.getRating();
        }
        throw new IllegalArgumentException("rating must be either 1 or -1");
    }

    /**
     * Remove a comment with the given id
     *
     * @param id of the comment
     */
    @Transactional
    public void removeComment(final Long id) throws CommentNotFoundException {
        notNull(id, "id cannot be null");

        Comment comment = em.find(Comment.class, id);
        if (comment == null) {
            LOG.info("Found no comment with id {}", id);
            throw new CommentNotFoundException(id);
        }

        LOG.info("Remove comment with id {}", id);

        em.remove(comment);
    }
}
