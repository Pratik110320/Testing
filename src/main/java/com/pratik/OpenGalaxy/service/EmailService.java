package com.pratik.OpenGalaxy.service;

import com.pratik.OpenGalaxy.model.Problem;
import com.pratik.OpenGalaxy.model.Solution;
import com.pratik.OpenGalaxy.model.User;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserService userService;               // <-- resolve IDs to Users
    private final String fromEmail = "ishanresso@gmail.com";

    public EmailService(JavaMailSender mailSender, UserService userService) {
        this.mailSender = mailSender;
        this.userService = userService;
    }

    // ---------- Public API ----------

    // 1) Problem owner (postedBy) gets an email when their problem is posted
    public void sendProblemPostedAlert(Problem problem) {
        User poster = userService.getUserById(problem.getPostedBy());
        if (poster == null || isBlank(poster.getEmail())) return;

        String subject = "🚀 Problem Posted Successfully on OpenGalaxy";
        String body =
                "Hi " + displayName(poster) + ",\n\n" +
                        "Your problem titled \"" + nz(problem.getTitle()) + "\" has been successfully posted.\n\n" +
                        "Best,\nOpenGalaxy Team";

        send(poster.getEmail(), subject, body);
    }

    // 2) Problem owner (postedBy) gets an email when a solution is submitted
    public void sendSolutionSubmittedAlert(Solution solution, Problem problem) {
        User problemOwner = userService.getUserById(problem.getPostedBy());
        User submitter    = userService.getUserById(solution.getSubmittedBy());
        if (problemOwner == null || isBlank(problemOwner.getEmail())) return;

        String subject = "✨ New Solution Submitted to Your Problem";
        String body =
                "Hi " + displayName(problemOwner) + ",\n\n" +
                        displayName(submitter) + " just submitted a solution to your problem \"" +
                        nz(problem.getTitle()) + "\".\n\n" +
                        "Log in to OpenGalaxy to review it.\n\n" +
                        "Best,\nOpenGalaxy Team";

        send(problemOwner.getEmail(), subject, body);
    }

    // 3) Solution submitter (submittedBy) gets an email when their solution is accepted
    public void sendSolutionAcceptedAlert(Solution solution, Problem problem) {
        User submitter   = userService.getUserById(solution.getSubmittedBy());
        User problemOwner = userService.getUserById(problem.getPostedBy()); // for name in body
        if (submitter == null || isBlank(submitter.getEmail())) return;

        String subject = "🎉 Your Solution Has Been Accepted!";
        String body =
                "Hi " + displayName(submitter) + ",\n\n" +
                        "Congrats! Your solution for \"" + nz(problem.getTitle()) + "\" was accepted"
                        + (problemOwner != null ? " by " + displayName(problemOwner) : "") + ".\n\n" +
                        "Keep contributing!\n\n" +
                        "Best,\nOpenGalaxy Team";

        send(submitter.getEmail(), subject, body);
    }

    // ---------- Helpers ----------

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("Email send failed: " + e.getMessage());
        }
    }

    private static String displayName(User u) {
        if (u == null) return "there";
        String n = u.getFullName();
        if (isBlank(n)) n = u.getUsername();
        return isBlank(n) ? "there" : n;
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private static String nz(String s) { return s == null ? "" : s; }
}
