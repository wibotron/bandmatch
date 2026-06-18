package com.bandmatch.bandmatch.controller;

import com.bandmatch.bandmatch.domain.user.User;
import com.bandmatch.bandmatch.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    // Halaman utama -> redirect ke register
    @GetMapping("/")
    public String home() {
        return "redirect:/register";
    }

    // TAMPILKAN FORM REGISTRASI
    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    // PROSES REGISTRASI
    @PostMapping("/register")
    public String processRegister(@RequestParam String name,
                                  @RequestParam String email,
                                  @RequestParam String password,
                                  @RequestParam String role,
                                  @RequestParam(required = false) String companyPosition,
                                  @RequestParam(required = false) String stageName,
                                  @RequestParam(required = false) String primaryInstrument,
                                  Model model) {
        try {
            User registeredUser;
            if ("manager".equals(role)) {
                registeredUser = authService.registerManager(name, email, password, companyPosition);
            } else {
                registeredUser = authService.registerBandMember(name, email, password, stageName, primaryInstrument);
            }

            model.addAttribute("userName", registeredUser.getName());
            model.addAttribute("dashboardUrl", registeredUser.getDashboardRedirectUrl());
            model.addAttribute("role", role);
            return "register-success";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    // TAMPILKAN FORM LOGIN
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    // PROSES LOGIN (SIMPAN KE SESSION)
    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        try {
            User loggedInUser = authService.login(email, password);

            // Simpan user ke session (ini pengganti Spring Security yang simpel)
            session.setAttribute("userId", loggedInUser.getId());
            session.setAttribute("userRole", loggedInUser.getPermissions().contains("MANAGE_BAND") ? "manager" : "member");
            session.setAttribute("userName", loggedInUser.getName());

            // Redirect langsung ke dashboard (polymorphism!)
            return "redirect:" + loggedInUser.getDashboardRedirectUrl();

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    // LOGOUT
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}