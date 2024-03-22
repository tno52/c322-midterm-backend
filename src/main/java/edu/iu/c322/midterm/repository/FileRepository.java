package edu.iu.c322.midterm.repository;

import edu.iu.c322.midterm.model.Question;
import edu.iu.c322.midterm.model.Quiz;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class FileRepository {
    private String IMAGES_FOLDER_PATH = "quizzes/questions/images";
    private static final String NEW_LINE = System.lineSeparator();
    private static final String QUESTION_DATABASE_NAME = "quizzes/questions.txt";
    private static final String QUIZ_DATABASE_NAME = "quizzes/quizzes.txt";

    public FileRepository() {
        File imagesDirectory = new File(IMAGES_FOLDER_PATH);
        if(!imagesDirectory.exists()) {
            imagesDirectory.mkdirs();
        }
    }

    private static void appendToFile(Path path, String content)
            throws IOException {
        Files.write(path,
                content.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    public int add(Question question) throws IOException {
        Path path = Paths.get(QUESTION_DATABASE_NAME);
        List<Question> questions = findAllQuestions();
        int id = 0;
        for(Question q : questions) {
            if(q.getId() > id) {
                id = q.getId();
            }
        }
        id = id + 1;
        question.setId(id);
        String data = question.toLine();
        appendToFile(path, data + NEW_LINE);
        return id;
    }





    public List<Question> findAllQuestions() throws IOException {
        List<Question> result = new ArrayList<>();
        Path path = Paths.get(QUESTION_DATABASE_NAME);
        if (Files.exists(path)) {
            List<String> data = Files.readAllLines(path);
            for (String line : data) {
                if(line.trim().length() != 0) {
                    Question q = Question.fromLine(line);
                    result.add(q);
                }
            }
        }
        return result;
    }





    public List<Question> find(String answer) throws IOException {
        List<Question> animals = findAllQuestions();
        List<Question> result = new ArrayList<>();
        for (Question question : animals) {
            if (answer != null && !question.getAnswer().trim().equalsIgnoreCase(answer.trim())) {
                continue;
            }
            result.add(question);
        }
        return result;
    }

    public List<Question> find(List<Integer> ids) throws IOException {
        List<Question> questions = findAllQuestions();
        List<Question> result = new ArrayList<>();
        for (int id : ids) {
            Question q = questions.stream().filter(x -> x.getId() == id).toList().get(0);
            result.add(q);
        }
        return result;
    }



    public Question get(Integer id) throws IOException {
        List<Question> questions = findAllQuestions();
        for (Question question : questions) {
            if (question.getId() == id) {
                return question;
            }
        }
        return null;
    }

    public boolean updateImage(int id, MultipartFile file) throws IOException {
        System.out.println(file.getOriginalFilename());
        System.out.println(file.getContentType());

        String fileExtension = ".png";
        Path path = Paths.get(IMAGES_FOLDER_PATH
                + "/" + id + fileExtension);
        System.out.println("The file " + path + " was saved successfully.");
        file.transferTo(path);
        return true;
    }

    public byte[] getImage(int id) throws IOException {
        String fileExtension = ".png";
        Path path = Paths.get(IMAGES_FOLDER_PATH
                + "/" + id + fileExtension);
        byte[] image = Files.readAllBytes(path);
        return image;
    }


    public int addQuiz(Quiz quiz) throws IOException {
        Path path = Paths.get(QUIZ_DATABASE_NAME);
        List<Quiz> quizzes = findAllQuizzes();
        int id = quizzes.stream().mapToInt(Quiz::getId).max().orElse(0) + 1;
        quiz.setId(id);
        String data = quizToLine(quiz);
        appendToFile(path, data + NEW_LINE);
        return id;
    }

    public List<Quiz> findAllQuizzes() throws IOException {
        List<Quiz> result = new ArrayList<>();
        Path path = Paths.get(QUIZ_DATABASE_NAME);
        if (Files.exists(path)) {
            List<String> data = Files.readAllLines(path);
            for (String line : data) {
                if (!line.trim().isEmpty()) {
                    Quiz quiz = quizFromLine(line);
                    result.add(quiz);
                }
            }
        }
        return result;
    }

    public Quiz findQuizById(int id) throws IOException {
        return findAllQuizzes().stream()
                .filter(quiz -> quiz.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public boolean updateQuiz(int id, Quiz newQuizData) throws IOException {
        List<Quiz> quizzes = findAllQuizzes();
        boolean found = false;
        for (int i = 0; i < quizzes.size(); i++) {
            if (quizzes.get(i).getId() == id) {
                if (newQuizData.getTitle() != null) {
                    quizzes.get(i).setTitle(newQuizData.getTitle());
                }
                if (newQuizData.getQuestionIds() != null) {
                    quizzes.get(i).setQuestionIds(newQuizData.getQuestionIds());
                }
                found = true;
                break;
            }
        }
        if (found) {
            rewriteQuizzesDatabase(quizzes);
            return true;
        }
        return false;
    }

    private String quizToLine(Quiz quiz) {
        String questionIds = quiz.getQuestionIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return String.format("%d,%s,%s", quiz.getId(), quiz.getTitle(), questionIds);
    }

    private Quiz quizFromLine(String line) {
        String[] tokens = line.split(",");
        int id = Integer.parseInt(tokens[0].trim());
        String title = tokens[1].trim();
        List<Integer> questionIds = Arrays.stream(tokens, 2, tokens.length)
                .map(String::trim)
                .map(Integer::valueOf)
                .collect(Collectors.toList());
        return new Quiz(id, title, questionIds);
    }

    private void rewriteQuizzesDatabase(List<Quiz> quizzes) throws IOException {
        Path path = Paths.get(QUIZ_DATABASE_NAME);
        String data = quizzes.stream()
                .map(this::quizToLine)
                .collect(Collectors.joining(NEW_LINE));
        Files.write(path, data.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }



}
