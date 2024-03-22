package edu.iu.c322.midterm.controllers;

import edu.iu.c322.midterm.model.Question;
import edu.iu.c322.midterm.model.Quiz;
import edu.iu.c322.midterm.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/quizzes")
public class QuizController {

    private final FileRepository fileRepository;

    @Autowired
    public QuizController(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @PostMapping
    public ResponseEntity<?> addQuiz(@RequestBody Quiz quiz) {
        try {
            int id = fileRepository.addQuiz(quiz);
            return ResponseEntity.status(HttpStatus.CREATED).body(id);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add quiz: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllQuizzes() {
        try {
            List<Quiz> quizzes = fileRepository.findAllQuizzes();
            quizzes.forEach(this::loadQuestionsForQuiz);
            return new ResponseEntity<>(quizzes, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve quizzes: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuiz(@PathVariable int id) {
        try {
            Quiz quiz = fileRepository.findQuizById(id);
            if (quiz != null) {
                loadQuestionsForQuiz(quiz);
                return new ResponseEntity<>(quiz, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Quiz not found", HttpStatus.NOT_FOUND);
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve quiz: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuiz(@PathVariable int id, @RequestBody Quiz quiz) {
        try {
            boolean updated = fileRepository.updateQuiz(id, quiz);
            if (updated) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Quiz not found", HttpStatus.NOT_FOUND);
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update quiz: " + e.getMessage());
        }
    }

    private void loadQuestionsForQuiz(Quiz quiz) {
        try {
            System.out.println("Loading questions for quiz ID: " + quiz.getId() + " with question IDs: " + quiz.getQuestionIds());
            List<Question> questions = fileRepository.find(quiz.getQuestionIds());
            System.out.println("Found questions: " + questions.size());
            quiz.setQuestions(questions);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load questions for quiz: " + e.getMessage());
        }
    }
}

