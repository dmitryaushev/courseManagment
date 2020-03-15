package com.courses.management.common;

import com.courses.management.common.commands.Exit;
import com.courses.management.common.commands.Help;
import com.courses.management.common.commands.util.InputString;
import com.courses.management.course.*;
import com.courses.management.user.CreateUser;
import com.courses.management.user.FindUserByEmail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

public class MainController {
    private static final Logger LOG = LogManager.getLogger(MainController.class);
    private View view;
    private List<Command> commands;

    public MainController(View view) {
        this.view = view;
        this.commands = Arrays.asList(
                new CreateCourse(view),
                new FindCourseByID(view),
                new FindCourseByTitle(view),
                new FindCoursesByStatus(view),
                new FindAllCourses(view),
                new UpdateCourse(view),
                new UpdateCourse(view),
                new DeleteCourse(view),
                new CreateUser(view),
                new FindUserByEmail(view),
                new Help(view),
                new Exit(view)
        );
    }

    public void read() {
        view.write("Welcome");
        while (true) {
            view.write("Enter a command or use help to list all available commands:");
            String read = view.read();
            doCommand(read);
        }
    }

    private void doCommand(String input) {
    LOG.debug(String.format("doCommand: input=%s", input));
        InputString entry = new InputString(input);
        for (Command command : commands) {
            try {
                if (command.canProcess(entry)) {
                    command.process(entry);
                    break;
                }
            } catch (Exception e) {
                LOG.warn(String.format("doCommand. WARN %s", e.getMessage()), e);
                printError(e);
                break;
            }
        }
    }

    private void printError(Exception e) {
        String message = e.getMessage();
        view.write("Error because of " + message);
        view.write("Please try again");
    }
}
