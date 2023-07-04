package com.krokochik.ideasForum.model;

import com.krokochik.ideasForum.service.crypto.TokenService;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"avatar", "id"})
@Entity
@Table(name = "usr")
public class User
{
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id;
    Long oauth2Id;

    // absolutes
    @NotNull
    String username;
    @NotNull
    String nickname;
    @NotNull
    String email;
    @NotNull
    String password;

    // tokens
    String mailConfirmationToken;
    String passwordAbortToken;
    String mfaCode;
    String mfaToken;

    // flags
    boolean active = true;
    boolean confirmMailSent = false;
    boolean passwordAbortSent = false;
    boolean mfaConnected = false;

    // images
    byte[] avatar = "iVBORw0KGgoAAAANSUhEUgAAAOEAAADLCAIAAAAX5mN9AAABJmlDQ1BBZG9iZSBSR0IgKDE5OTgpAAAoz2NgYDJwdHFyZRJgYMjNKykKcndSiIiMUmA/z8DGwMwABonJxQWOAQE+IHZefl4qAwb4do2BEURf1gWZxUAa4EouKCoB0n+A2CgltTiZgYHRAMjOLi8pAIozzgGyRZKywewNIHZRSJAzkH0EyOZLh7CvgNhJEPYTELsI6Akg+wtIfTqYzcQBNgfClgGxS1IrQPYyOOcXVBZlpmeUKBhaWloqOKbkJ6UqBFcWl6TmFit45iXnFxXkFyWWpKYA1ULcBwaCEIWgENMAarTQZKAyAMUDhPU5EBy+jGJnEGIIkFxaVAZlMjIZE+YjzJgjwcDgv5SBgeUPQsykl4FhgQ4DA/9UhJiaIQODgD4Dw745AMDGT/0ZOjZcAAAACXBIWXMAACKaAAAimgG+3fsqAAAF8WlUWHRYTUw6Y29tLmFkb2JlLnhtcAAAAAAAPD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4gPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0iQWRvYmUgWE1QIENvcmUgNi4wLWMwMDIgMTE2LjE2NDc2NiwgMjAyMS8wMi8xOS0yMzoxMDowNyAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RFdnQ9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZUV2ZW50IyIgeG1sbnM6ZGM9Imh0dHA6Ly9wdXJsLm9yZy9kYy9lbGVtZW50cy8xLjEvIiB4bWxuczpwaG90b3Nob3A9Imh0dHA6Ly9ucy5hZG9iZS5jb20vcGhvdG9zaG9wLzEuMC8iIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIDIxLjIgKFdpbmRvd3MpIiB4bXA6Q3JlYXRlRGF0ZT0iMjAyMi0wOC0yMlQwMDoyNjowMiswMzowMCIgeG1wOk1ldGFkYXRhRGF0ZT0iMjAyMi0wOC0yMlQwMDoyNjowMiswMzowMCIgeG1wOk1vZGlmeURhdGU9IjIwMjItMDgtMjJUMDA6MjY6MDIrMDM6MDAiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6Zjg2YjljYWEtYWUzYS0yMDQwLWE5ZGItNjdmODlkNjYwZTlkIiB4bXBNTTpEb2N1bWVudElEPSJhZG9iZTpkb2NpZDpwaG90b3Nob3A6ZmU3ZWU3NmMtY2YzZS1lNzRmLWE4ODAtOTllMzg5MjhlYzM3IiB4bXBNTTpPcmlnaW5hbERvY3VtZW50SUQ9InhtcC5kaWQ6NmM5YzliYTctMDdmYS03ZTQxLTgwNDctZDY0OTJkODYxMzIxIiBkYzpmb3JtYXQ9ImltYWdlL3BuZyIgcGhvdG9zaG9wOkNvbG9yTW9kZT0iMyIgcGhvdG9zaG9wOklDQ1Byb2ZpbGU9IkFkb2JlIFJHQiAoMTk5OCkiPiA8eG1wTU06SGlzdG9yeT4gPHJkZjpTZXE+IDxyZGY6bGkgc3RFdnQ6YWN0aW9uPSJjcmVhdGVkIiBzdEV2dDppbnN0YW5jZUlEPSJ4bXAuaWlkOjZjOWM5YmE3LTA3ZmEtN2U0MS04MDQ3LWQ2NDkyZDg2MTMyMSIgc3RFdnQ6d2hlbj0iMjAyMi0wOC0yMlQwMDoyNjowMiswMzowMCIgc3RFdnQ6c29mdHdhcmVBZ2VudD0iQWRvYmUgUGhvdG9zaG9wIDIxLjIgKFdpbmRvd3MpIi8+IDxyZGY6bGkgc3RFdnQ6YWN0aW9uPSJzYXZlZCIgc3RFdnQ6aW5zdGFuY2VJRD0ieG1wLmlpZDpmODZiOWNhYS1hZTNhLTIwNDAtYTlkYi02N2Y4OWQ2NjBlOWQiIHN0RXZ0OndoZW49IjIwMjItMDgtMjJUMDA6MjY6MDIrMDM6MDAiIHN0RXZ0OnNvZnR3YXJlQWdlbnQ9IkFkb2JlIFBob3Rvc2hvcCAyMS4yIChXaW5kb3dzKSIgc3RFdnQ6Y2hhbmdlZD0iLyIvPiA8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".getBytes();
    byte[] qrcode = null;

    // collections
    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    Set<Role> roles;

    @ElementCollection(targetClass = String.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "mfa_reset_token", joinColumns = @JoinColumn(name = "user_id"))
    Set<String> mfaResetTokens;

    public User(@NotNull @NonNull String username, @NotNull @NonNull String email, @NotNull @NonNull String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        nickname = username;
    }

    @Transient
    @Autowired
    TokenService tokenService;

    public void startMfaCodeGenerating() {
        if (mfaConnected) {
            Timer generatorsTimer = new Timer();
            generatorsTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mfaCode = tokenService.generateMfaCode();
                }
            }, 30 * 1000);
        }
    }
}
