package com.krokochik.ideasForum.controller;

import com.krokochik.ideasForum.model.Mail;
import com.krokochik.ideasForum.model.Role;
import com.krokochik.ideasForum.model.User;
import com.krokochik.ideasForum.repository.UserRepository;
import com.krokochik.ideasForum.service.UserValidationService;
import com.krokochik.ideasForum.service.crypto.TokenService;
import com.krokochik.ideasForum.service.MailService;
import com.krokochik.ideasForum.service.jdbc.UserService;
import com.krokochik.ideasForum.service.security.SecurityRoutineProvider;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Collections;

@Controller
public class EmailController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    MailService mailService;

    @Autowired
    SecurityRoutineProvider srp;

    @GetMapping("/email-validity-confirmation")
    public String checkingIfUserEmailIsOk(@RequestParam(name = "newEmail", required = false) String mode, HttpSession session, Model model) {
        Object temp = null;
        String newEmail = "";
        try {
            temp = session.getAttribute("newEmail");
        } catch (NullPointerException exception) {
            exception.printStackTrace();
        }
        if (srp.hasRole(Role.USER) && (mode == null || temp == null))
            return "redirect:/main";

        if (temp != null)
            newEmail = temp.toString();

        val ctx = srp.getContext();

        User user = userRepository.findByUsername(ctx.getAuthentication().getName());
        if (srp.hasRole(Role.ANONYM) || srp.hasRole(Role.USER)) {
            if (!user.isConfirmMailSent()) {
                String userToken = new TokenService().generateToken();
                userRepository.setMailConfirmationTokenById(userToken, user.getId());

                boolean isEmailChanging = !newEmail.isEmpty();
                boolean isAnonym = srp.hasRole(Role.ANONYM);
                String finalNewEmail = newEmail;

                Thread mailSending = new Thread(() -> {
                    Mail mail = new Mail();
                    mail.setReceiver(isEmailChanging ? finalNewEmail : user.getEmail());
                    mail.setTheme("Подтверждение почты");
                    mail.setLink("https://ideas-forum.herokuapp.com/confirm?name=" + ctx.getAuthentication().getName() +
                            "&token=" + userToken + (isEmailChanging ? "&newEmail=" + finalNewEmail : ""));
                    mailService.sendConfirmationMail(mail, user.getUsername(), isAnonym ? "На вашу почту был зарегестрирован новый аккаунт." : "К вашей почте был привязан аккаунт.");
                });
                mailSending.start();
                userRepository.setConfirmMailSentById(true, user.getId());
            }
            model.addAttribute("newEmail", newEmail);
            return "email-confirmation-instructions";
        }

        if (srp.isAuthenticated())
            return "redirect:/main";
        return "redirect:/login";
    }

    @GetMapping("/confirm")
    public String confirmEmail(@RequestParam(name = "name") String name,
                               @RequestParam(name = "token") String token,
                               @RequestParam(name = "newEmail", required = false) String newEmail,
                               HttpSession session, Model model) {
        User user = userRepository.findByUsername(name);
        if (user != null && user.getMailConfirmationToken().equals(token)) {
            if (newEmail != null && srp.hasRole(Role.USER)) {
                userRepository.setEmailById(newEmail, user.getId());
                session.removeAttribute("newEmail");
                return "redirect:/settings";
            }

            userService.setRolesById(user.getId(), Collections.singleton(Role.USER));
            user.setRoles(Collections.singleton(Role.USER));
            srp.authorizeUser(SecurityContextHolder.getContext(), user);

            return "redirect:/main";

        }
        return "redirect:/main";
    }

    @PostMapping("/change-email")
    public String changeEmail(Model model,
                              @RequestParam(name = "email") String email,
                              @RequestParam(name = "password", required = false) String password,
                              HttpSession session) {

        User user = userRepository.findByUsername(srp.getContext().getAuthentication().getName());

        if (email.isEmpty() || !UserValidationService.validateEmail(email) ||
                (user.getRoles().contains(Role.USER) && !password.equals(user.getPassword())))
            return "redirect:/change-email?error";

        userRepository.setConfirmMailSentById(false, user.getId());

        session.setAttribute("newEmail", email);
        return "redirect:/email-validity-confirmation?newEmail";
    }

    @GetMapping("/change-email")
    public String changeEmail(Model model, HttpSession session,
                                 @RequestParam(name = "from", required = false) String from) {

        if (session.getAttribute("newEmail") != null)
            return "redirect:/email-validity-confirmation?newEmail";

        if (srp.hasRole(Role.ANONYM))
            model.addAttribute("isAnonym", true);
        else
            model.addAttribute("isAnonym", false);

        if (from != null)
            model.addAttribute("from", "/" + from);

        return "email-change";
    }
}
