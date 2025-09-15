package com.team.OpenGalaxy.service;

import com.team.OpenGalaxy.model.Problem;
import com.team.OpenGalaxy.model.Solution;
import com.team.OpenGalaxy.model.User;
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
    // Call this after successful login via GitHub
    public void sendLoginSuccessAlert(User user) {
        if (user == null || isBlank(user.getEmail())) return;

        String subject = " Your mission begins now, Hi Dev — OpenGalaxy ready for your moves";
        String body =
                "Hey " + displayName(user) + ",\n\n" +
                        "Looks like GitHub finally decided to share you with us again 😉.\n" +
                        "You’ve successfully logged into OpenGalaxy.\n\n" +
                        "The stage is all yours, Dev — welcome to OpenGalaxy, where new coders morph into Codebreakers by forking, fixing, and flexing their code..\n"  +
                        "So Just FORK IT, FIX IT & FLEX IT😎\n\n"+
                        "— Truly yours, OpenGalaxy Team";


        send(user.getEmail(), subject, body);
    }


    // 1) Problem owner (postedBy) gets an email when their problem is posted
    public void sendProblemPostedAlert(Problem problem) {
        User poster = userService.getUserById(problem.getPostedBy());
        if (poster == null || isBlank(poster.getEmail())) return;

        String subject = "🚀 Your problem just went live!";
        String body =
                "Hi " + displayName(poster) + ",\n\n" +
                        "Boom! 💥 Your problem  \"" + nz(problem.getTitle()) + "\" has just been posted to OpenGalaxy.\n" +
                        "Now sit back, relax, and let the world solve it (or struggle with it) for you 😏.\n\n" +
                        "Best,\nTeam OpenGalaxy";


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
                        displayName(submitter) + " just dropped a solution for your problem \"" +
                        nz(problem.getTitle()) + "\".\n" +
                        "Time to check it out and see if they cracked the code 🔐.\n\n" +
                        "Stay curious,\nOpenGalaxy Team";

        send(problemOwner.getEmail(), subject, body);
    }

    // 3) Solution submitter (submittedBy) gets an email when their solution is accepted
    public void sendSolutionAcceptedAlert(Solution solution, Problem problem) {
        User submitter   = userService.getUserById(solution.getSubmittedBy());
        User problemOwner = userService.getUserById(problem.getPostedBy()); // for name in body
        if (submitter == null || isBlank(submitter.getEmail())) return;

        String subject = "Update: Your, Response just got accepted ✅";
        String body =
                "Yo " + displayName(submitter) + ",\n\n" +
                        "Accepted — and not in a quiet way. Your solution for \"" + nz(problem.getTitle()) + "\" absolutely nailed it. 🔥\n\n" +
                        "You came, you debugged, you conquered. That code wasn’t just right — it was ruthless.\n" +
                        "Time to flex this one on your feed.\n\n" +
                        "— Team OpenGalaxy\nFork it. Fix it. Flex it.";

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
