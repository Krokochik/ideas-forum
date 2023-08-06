package com.krokochik.ideasforum.controller.account;

import com.krokochik.ideasforum.model.db.User;
import com.krokochik.ideasforum.model.functional.Role;
import com.krokochik.ideasforum.model.service.Mail;
import com.krokochik.ideasforum.service.MailService;
import com.krokochik.ideasforum.service.UserValidator;
import com.krokochik.ideasforum.service.crypto.TokenService;
import com.krokochik.ideasforum.service.jdbc.UserService;
import com.krokochik.ideasforum.service.security.SecurityRoutineProvider;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Controller
public class EmailController {

    @Autowired
    UserService userService;

    @Autowired
    MailService mailService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    SecurityRoutineProvider srp;

    @GetMapping("/email-validity-confirmation")
    public String checkingIfUserEmailIsOk(HttpSession session, Model model,
                                          @RequestParam(name = "newEmail", required = false) String mode) {
        Object temp = null;
        String newEmail = "";
        try {
            temp = session.getAttribute("newEmail");
        } catch (NullPointerException exc) {
            log.error("An error occurred", exc);
        }
        if (srp.hasRole(Role.USER) && (mode == null || temp == null))
            return "redirect:/main";

        if (temp != null)
            newEmail = temp.toString();

        val ctx = srp.getContext();

        User user = userService
                .findByUsernameOrUnknown(ctx.getAuthentication().getName());
        if ((srp.hasRole(Role.ANONYM) || srp.hasRole(Role.USER)) && !user.getEmail().equals("unknown")) {
            if (!user.isConfirmMailSent()) {
                String userToken = new TokenService().generateToken();
                userService.setMailConfirmationTokenById(userToken, user.getId());

                boolean isEmailChanging = !newEmail.isBlank();
                boolean isAnonym = srp.hasRole(Role.ANONYM);
                String finalNewEmail = newEmail;

                new Thread(() -> {
                    Mail mail = new Mail();
                    mail.setReceiver(isEmailChanging ? finalNewEmail : user.getEmail());
                    mail.setTheme("Подтверждение почты");
                    mail.setLink("https://ideas-forum.herokuapp.com/confirm?name=" + ctx.getAuthentication().getName() +
                            "&token=" + userToken + (isEmailChanging ? "&newEmail=" + finalNewEmail : ""));
                    try {
                        mailService.sendConfirmationMail(mail, user.getUsername(), isAnonym
                                ? "На вашу почту был зарегестрирован новый аккаунт."
                                : "К вашей почте был привязан аккаунт.");
                        userService.setConfirmMailSentById(true, user.getId());
                    } catch (MessagingException e) {
                        log.error("", e);
                    }
                }).start();
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
        Optional<User> userOptional = userService.findByUsername(name);
        User user = userOptional.orElse(null);
        if (userOptional.isPresent() && user.getMailConfirmationToken().equals(token)) {
            if (newEmail != null && srp.hasRole(Role.USER)) {
                userService.setEmailById(newEmail, user.getId());
                session.removeAttribute("newEmail");
                return "redirect:/settings";
            }

            userService.setRolesById(user.getId(), Collections.singleton(Role.USER));
            user.setRoles(Collections.singleton(Role.USER));
            srp.authorizeUser(user, SecurityContextHolder.getContext());

            return "redirect:/main";

        }
        return "redirect:/email-validity-confirmation";
    }

    @PostMapping("/change-email")
    public String changeEmail(Model model, HttpSession session,
                              @RequestParam(name = "email") String email,
                              @RequestParam(name = "password", required = false) String password) {

        Optional<User> userOptional = userService.findByUsername(srp.getContext().getAuthentication().getName());
        User user = userOptional.orElse(null);

        if (userOptional.isEmpty() || email.isBlank() || !UserValidator.validateEmail(email) ||
                (user.getRoles().contains(Role.USER) &&
                        !passwordEncoder.matches(password, user.getPassword())))
            return "redirect:/change-email?error";

        userService.setConfirmMailSentById(false, user.getId());

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
