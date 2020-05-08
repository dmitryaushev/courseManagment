package com.courses.management.homework;

import org.apache.commons.fileupload.FileItem;

import java.util.List;

public interface HomeworkService {

    void uploadFile(List<FileItem> items, int courseId);
    Homework getHomework(int id);
}
