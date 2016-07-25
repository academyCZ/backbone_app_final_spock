package org.academy.java.service;


import org.academy.java.entity.Answer;
import org.academy.java.entity.Interview;
import org.academy.java.entity.Question;
import org.academy.java.entity.Question.QuestionType;
import org.academy.java.repository.AnswerRepository;
import org.academy.java.repository.InterviewRepository;
import org.academy.java.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.academy.java.entity.Question.QuestionType.*;

@Service
public class QuestionService {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private InterviewRepository interviewRepository;


    @Transactional
    public Question saveOrUpdateQuestion(Question question) {
        if (question.getQuestionType().equals(Question.QuestionType.TEXT_AREA) && question.getAnswers().isEmpty()) {
            return makeTextAnswerForQuestion(question);
        }
        return questionRepository.save(question);
    }

    public Question addAnswer(Question question, Answer answer) {
        if (question.getQuestionType() == Question.QuestionType.TEXT_AREA) {
            return question;
        }
        question.getAnswers().add(answer);
        answer.setQuestion(question);
        answerRepository.save(answer);
        return questionRepository.save(question);
    }

    @Transactional
    public List<Question> getQuestionsByTypes(QuestionType... questionTypes) {
        return questionRepository.getByQuestionTypes(questionTypes);
    }

    @Transactional
    public Question makeCheckboxAnswerForQuestion(Question question) {

        question.setQuestionType(CHECKBOX);

        return questionRepository.save(question);
    }

    @Transactional
    public Question makeTextAnswerForQuestion(Question question) {

        answerRepository.deleteAllByQuestionId(question.getId());
        Answer a = new Answer();
        a.setQuestion(question);
        a.setText("");
        answerRepository.save(a);

        question.setQuestionType(TEXT_AREA);

        return questionRepository.save(question);
    }

    @Transactional
    public Question makeRadioAnswersForQuestion(Question question, Long chosenAnswerId) {

        question.setQuestionType(RADIO);

        if (question.getAnswers().size() == 0) {

            question.getAnswers().add(
                    new Answer().setCorrect(true).setQuestion(question)
            );
            return questionRepository.save(question);
        }

        Long idOfChosenRadioAnswer = chosenAnswerId != null
                ? chosenAnswerId
                : question.getAnswers().stream().filter(answer -> answer.isCorrect()).findFirst()
                        .orElse(question.getAnswers().stream().findFirst().get())
                        .getId();

        question.getAnswers().stream().forEach(
                a -> {
                    a.setCorrect(a.getId() == idOfChosenRadioAnswer);
                }
        );

        return questionRepository.save(question);
    }

    @Transactional(readOnly = true)
    public Question findQuestionById(Long questionId) {
        return questionRepository.findOne(questionId);
    }

    @Transactional(readOnly = true)
    public Iterable<Question> findAllQuestions() {
        return questionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Answer findAnswerById(Long answerId) {
        return answerRepository.findOne(answerId);
    }

    public void deleteQuestion(Long id) {
        Question question = findQuestionById(id);
        Interview interview = question.getInterview();
        interview.getQuestions().remove(question);
        interviewRepository.save(interview);
        questionRepository.delete(id);
    }
}
