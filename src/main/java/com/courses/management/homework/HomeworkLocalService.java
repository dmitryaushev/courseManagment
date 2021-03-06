package com.courses.management.homework;

import com.courses.management.course.Course;
import com.courses.management.course.CourseRepository;
import org.apache.commons.fileupload.FileItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@Profile("!aws")
public class HomeworkLocalService implements HomeworkService {

    private static final Logger LOG = LogManager.getLogger(HomeworkLocalService.class);
    private HomeworkRepository homeworkRepository;
    private CourseRepository courseRepository;
    private String folderPath;

    @Autowired
    public HomeworkLocalService(HomeworkRepository homeworkRepository, CourseRepository courseRepository) {
        this.homeworkRepository = homeworkRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public Homework getHomework(int id) throws IOException {
        LOG.debug(String.format("getHomework: id=%d", id));
        Homework homework = homeworkRepository.findById(id).orElseThrow(() ->
                new FileNotFoundException("File doesn't found"));
        homework.setData(getFileInputStream(homework));
        return homework;
    }

    @Override
    public void uploadFile(List<FileItem> items, int courseId) {

        LOG.debug(String.format("uploadFile: courseId=%d", courseId));
        Course course = courseRepository.getOne(courseId);
        if (Objects.isNull(course)) {
            throw new RuntimeException(String.format("Course with id = %s not found", courseId));
        }

        Homework homework = null;
        try {
            for (FileItem item : items) {
                if (!item.isFormField()) {
                    homework = createHomework(course, item);
                    File file = new File(homework.getPath());
                    validateIfFileExists(file, homework.getTitle());
                    homeworkRepository.save(homework);
                    item.write(file);
                }
            }
        } catch (Exception e) {
            LOG.error(String.format("uploadFile: courseId=%d", courseId), e);
            if (Objects.nonNull(homework) && homework.getId() != 0) {
                homeworkRepository.delete(homework);
            }
            throw new RuntimeException("Error when loading file " + e.getMessage());
        }
    }

    private void validateIfFileExists(File file, String title) {
        if (file.exists()) {
            throw new RuntimeException(String.format("Homework with title %s already exist", title));
        }
    }

    private Homework createHomework(Course course, FileItem item) {

        LOG.debug(String.format("createHomework: courseId=%d, fileName=%s", course.getId(), item.getName()));
        String title = new File(item.getName()).getName();
        String path = String.format("%s%s%s%s%s", folderPath, File.separator, course.getTitle(), File.separator, title);

        Homework homework = new Homework();
        homework.setCourse(course);
        homework.setTitle(title);
        homework.setPath(path);
        return homework;
    }

    private FileInputStream getFileInputStream(Homework homework) throws FileNotFoundException {
        File file = new File(homework.getPath());
        if (!file.exists()) {
            throw new FileNotFoundException("No file found");
        }
        return new FileInputStream(file);
    }

    @Value("${folder_path}")
    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }
}
