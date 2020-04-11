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

package de.maxwell.qa.application.answer;

import de.maxwell.qa.domain.answer.Answer;
import de.maxwell.qa.domain.answer.AnswerNotFoundException;
import de.maxwell.qa.domain.answer.AnswerService;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static de.maxwell.qa.infrastructure.helper.JWTCheck.checkJWT;

@Path("answer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AnswerResource {
    private static final Logger LOG = LoggerFactory.getLogger(AnswerResource.class);

    @Inject
    MetricRegistry metricRegistry;

    @Inject
    AnswerService service;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/{id}")
    @Counted(name = "get_answer_total", description = "get one answer counter")
    @Timed(name = "get_answer_timer", description = "Time to get one answer", unit = MetricUnits.SECONDS)
    public Response getAnswer(@PathParam("id") final Long answerID) {
        try {
            LOG.info("Find answer with ID: {}", answerID);

            Answer answer = this.service.findAnswer(answerID);

            return Response.ok()
                    .entity(answer)
                    .build();
        } catch (AnswerNotFoundException q) {
            LOG.info("Could not find answer with ID: {}", answerID);
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        } catch (NullPointerException n) {
            LOG.info("Arguments have errors {}", n.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .build();
        } catch (Exception e) {
            Counter get_answer_500 = metricRegistry.counter("get_answer_500");
            get_answer_500
                    .inc();

            LOG.info("An error occured");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @GET
    @Counted(name = "list_answers_by_question_id_total", description = "list answers by question counter")
    @Timed(name = "list_answers_by_question_id_timer", description = "Time to list answers by question", unit = MetricUnits.SECONDS)
    public Response listQuestionsPaginated(@Size(min = 0) @QueryParam("questionID") final Long questionID, @Size(min = 0, max = 50) @QueryParam("limit") final Integer limit, @Size(min = 0) @QueryParam("offset") final Integer offset) {
        try {
            List<Answer> answers = this.service.findAnswersByQuestionID(questionID, limit, offset);
            LOG.info("Found {} answers of question with id: {}", limit * offset, questionID);

            return Response.ok()
                    .entity(answers)
                    .build();
        } catch (NullPointerException n) {
            LOG.info("Argument was not correct");
            return Response.status(Response.Status.BAD_REQUEST)
                    .build();
        } catch (Exception e) {
            Counter list_answers_by_question_id_total_500 = metricRegistry.counter("list_answers_by_question_id_total_500");
            list_answers_by_question_id_total_500
                    .inc();

            LOG.info("An error occured");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @GET
    @Counted(name = "list_answers_total", description = "list answers counter")
    @Timed(name = "list_answers_timer", description = "Time to list answers ", unit = MetricUnits.SECONDS)
    public Response listQuestionsPaginated(@Size(min = 0, max = 50) @QueryParam("limit") final Integer limit, @Size(min = 0) @QueryParam("offset") final Integer offset) {
        try {
            List<Answer> answers = this.service.findAnswers(limit, offset);
            LOG.info("Found {} answers", limit * offset);

            return Response.ok()
                    .entity(answers)
                    .build();
        } catch (NullPointerException n) {
            LOG.info("Argument was not correct");
            return Response.status(Response.Status.BAD_REQUEST)
                    .build();
        } catch (Exception e) {
            Counter list_answers_total_500 = metricRegistry.counter("list_answers_total_500");
            list_answers_total_500
                    .inc();

            LOG.info("An error occured");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @POST
    @Counted(name = "create_answer_total", description = "create one answer counter")
    @Timed(name = "create_answer_timer", description = "Time to create one answer", unit = MetricUnits.SECONDS)
    public Response createAnswer(final AnswerNewDTO baseAnswer) {
        try {
            LOG.info("Create new answer");

            boolean check = checkJWT(jwt, baseAnswer.getUserID());
            if (!check) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .build();
            }

            Answer answer = this.service.createAnswer(baseAnswer.getUserID(), baseAnswer.getQuestionID(), baseAnswer.getDescription());

            LOG.info("New answer with ID: {} created", answer.getId());

            return Response
                    .status(Response.Status.CREATED)
                    .entity(answer)
                    .build();
        } catch (NullPointerException n) {
            LOG.info("Wrong input for new answer");
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .build();
        }
    }

    @PUT
    @Counted(name = "update_answer_description_total", description = "update description of one answer counter")
    @Timed(name = "update_answer_description_timer", description = "Time to update description of  one answer", unit = MetricUnits.SECONDS)
    public Response updateDescription(final AnswerUpdateDescriptionDTO updateDescriptionDTO) {
        try {
            LOG.info("Update description of answer with id: {}", updateDescriptionDTO.getId());

            Answer answer = this.service.updateDescription(updateDescriptionDTO.getId(), updateDescriptionDTO.getNewDescription());

            return Response
                    .status(Response.Status.OK)
                    .entity(answer)
                    .build();
        } catch (IllegalArgumentException | NullPointerException n) {
            LOG.info("Wrong input for new answer");
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .build();
        }
    }

    @GET
    @Path("/{id}/increment")
    @Counted(name = "increment_rating_total", description = "increment rating of an answer counter")
    @Timed(name = "increment_rating_timer", description = "Time to increment rating of an answer", unit = MetricUnits.SECONDS)
    public Response incrementRating(@PathParam("id") final Long answerID) {
        try {
            LOG.info("Increment rating of answer with id: {}", answerID);

            Long rating = this.service.incrementRating(answerID);

            return Response
                    .status(Response.Status.OK)
                    .entity(rating)
                    .build();

        } catch (NullPointerException n) {
            LOG.info("Wrong input for increment rating");
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .build();
        }
    }

    @DELETE
    @Path("/{id}/decrement")
    @Counted(name = "decrement_rating_total", description = "decrement rating of an answer counter")
    @Timed(name = "decrement_rating_timer", description = "Time to decrement rating of an answer", unit = MetricUnits.SECONDS)
    public Response decrementRating(@PathParam("id") final Long answerID) {
        try {
            LOG.info("Decrement rating of answer with id: {}", answerID);

            Long rating = this.service.decrementRating(answerID);

            return Response
                    .status(Response.Status.OK)
                    .entity(rating)
                    .build();

        } catch (NullPointerException n) {
            LOG.info("Wrong input for increment rating");
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .build();
        }
    }

    @GET
    @Path("/{id}/correct")
    @Counted(name = "set_correct_answer_total", description = "set correct answer counter")
    @Timed(name = "set_correct_answer_timer", description = "Time to set correct answer", unit = MetricUnits.SECONDS)
    public Response setCorrectAnswer(@PathParam("id") final Long answerID) {
        try {
            LOG.info("Set correct answer of id: {}", answerID);

            this.service.setCorrectAnswer(answerID);

            return Response
                    .ok()
                    .build();
        } catch (NullPointerException n) {
            LOG.info("Wrong input for correct answer");
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .build();
        } catch (AnswerNotFoundException a) {
            LOG.info("Could not find answer");
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .build();
        }
    }

    @DELETE
    @Path("/{id}/incorrect")
    @Counted(name = "unset_correct_answer_total", description = "unset correct answer counter")
    @Timed(name = "unset_correct_answer_timer", description = "Time to unset correct answer", unit = MetricUnits.SECONDS)
    public Response unsetCorrectAnswer(@PathParam("id") final Long answerID) {
        try {
            LOG.info("Set incorrect answer of id: {}", answerID);

            this.service.unsetCorrectAnswer(answerID);

            return Response
                    .ok()
                    .build();
        } catch (NullPointerException n) {
            LOG.info("Wrong input for incorrect answer");
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .build();
        } catch (AnswerNotFoundException a) {
            LOG.info("Could not find answer");
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .build();
        }
    }
}
