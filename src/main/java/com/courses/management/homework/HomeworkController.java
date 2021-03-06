package com.courses.management.homework;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;

@Controller
@RequestMapping(path = "/homework/*")
public class HomeworkController {

    private static final Logger LOG = LogManager.getLogger(HomeworkController.class);
    private HomeworkService homeworkService;

    @Autowired
    public void setHomeworkService(HomeworkService homeworkService) {
        this.homeworkService = homeworkService;
    }

    @GetMapping(path = "/upload")
    public String getCreateHomeworkPage(@RequestParam(name = "course_id") String courseId, Model model) {
        model.addAttribute("courseId", courseId);
        return "create_homework";
    }

    @GetMapping(path = "/get")
    public void previewHomework(@RequestParam(name = "id") Integer id, HttpServletResponse response) throws IOException {
        Homework homework = homeworkService.getHomework(id);

        response.setHeader("Content-Type", URLConnection.guessContentTypeFromName(homework.getTitle()));
        response.setHeader("Content-Length", String.valueOf(homework.getData().available()));
        response.setHeader("Content-Disposition", String.format("inline; filename=\"%s\"", homework.getTitle()));
        homework.getData().transferTo(response.getOutputStream());
    }

    @GetMapping(path = "/preview")
    public String getPreviewPage(@RequestParam(name = "id") String id, Model model) {
        model.addAttribute("homeworkId", id);
        return "preview_homework";
    }

    @PostMapping(path = "/upload")
    public ModelAndView uploadHomework(@RequestParam(name = "course_id") Integer courseId,
                                       HttpServletRequest request, ModelMap model) {
        if (ServletFileUpload.isMultipartContent(request)) {
            try {
                List<FileItem> multiparts = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
                homeworkService.uploadFile(multiparts, courseId);
            } catch (Exception e) {
                LOG.error(String.format("uploadHomeWork: courseId=%d", courseId), e);
                model.addAttribute("error", "File upload failed dues to " + e);
                return new ModelAndView("create_homework", model);
            }
        } else {
            model.addAttribute("error", "No file found");
            return new ModelAndView("create_homework", model);
        }
        return new ModelAndView(String.format("redirect:/course/get?id=%s", courseId));
    }

    @ExceptionHandler({FileNotFoundException.class})
    public ModelAndView handleException(FileNotFoundException e) {
        LOG.error("handleException FileNotFoundException: ", e);
        ModelAndView modelAndView = new ModelAndView("file_not_found");
        modelAndView.addObject("error", e.getMessage());
        modelAndView.setStatus(HttpStatus.BAD_REQUEST);
        return modelAndView;
    }
}
