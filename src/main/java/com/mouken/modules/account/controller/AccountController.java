package com.mouken.modules.account.controller;

import com.mouken.modules.account.Account;
import com.mouken.modules.account.CurrentAccount;
import com.mouken.modules.account.ProviderUser;
import com.mouken.modules.account.dto.AccountCreateForm;
import com.mouken.modules.account.service.AccountService;
import com.mouken.modules.account.service.CustomUserDetailsService;
import com.mouken.modules.account.validator.AccountCreateFormValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountCreateFormValidator accountCreateFormValidator;
    private final AccountService accountService;
    private final CustomUserDetailsService customUserDetailsService;

    @InitBinder("accountCreateForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(accountCreateFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute(new AccountCreateForm());
        return "sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Validated AccountCreateForm form, BindingResult bindingResult) {

        if(bindingResult.hasErrors()) {
            return "sign-up";
        }
        customUserDetailsService.createAccount(form);
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, RedirectAttributes redirectAttributes) {

        // check account of email
        Account account = accountService.getAccountByEmail(email);

        if (account == null) {
            redirectAttributes.addFlashAttribute("error", "wrong.email");
            return "redirect:/check-email";
        }

        // check token
        if (!account.isValidToken(token)) {
            redirectAttributes.addFlashAttribute("error", "wrong.token");
            return "redirect:/check-email";
        }

        // verify account
        customUserDetailsService.completeSignUp(account);
        redirectAttributes.addFlashAttribute("info", "Complete");
        return "redirect:/check-email";
    }

    @GetMapping("/check-email")
    public String checkEmail(@CurrentAccount Account account, Model model) {

        if (account != null) {
            model.addAttribute(account);
            model.addAttribute("username", account.getUsername());
            model.addAttribute("email", account.getEmail());
        }

        return "account/check-email";
    }

    //todo refactor: naming to send confirm email
    @GetMapping("/send-email")
    public String sendEmail(@CurrentAccount Account account, Model model, RedirectAttributes redirectAttributes) {

        if (account.isEmailVerified()) {
            return "redirect:/";
        }

        if (!account.canSendConfirmEmail()) {
            redirectAttributes.addFlashAttribute("error", "You can get an email once per in 15 minutes");
            return "redirect:/check-email";
        }

        customUserDetailsService.sendSignUpConfirmEmail(account);
        redirectAttributes.addFlashAttribute("info", "Email has been sent");
        return "redirect:/check-email";
    }

    @GetMapping("profile/{username}")
    public String viewProfile(
            @PathVariable String username,
            @CurrentAccount Account account,
            Model model) {
        Account foundAccount = accountService.getAccountByUsername(username);
        log.info("currentUsername={}", account.getUsername());
        model.addAttribute(foundAccount);
        model.addAttribute("isOwner", foundAccount.equals(account));
        return "account/profile";
    }
/*
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final SignUpFormValidator signUpFormValidator;

    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Validated SignUpForm signUpForm, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "account/sign-up";
        }

        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);
        return "redirect:/check-email";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, RedirectAttributes redirectAttributes) {

        // check account of email
        Account account = accountRepository.findByEmail(email);

        if (account == null) {
            redirectAttributes.addFlashAttribute("error", "wrong.email");
            return "redirect:/check-email";
        }

        // check token
        if (!account.isValidToken(token)) {
            redirectAttributes.addFlashAttribute("error", "wrong.token");
            return "redirect:/check-email";
        }

        // verify account
        accountService.completeSignUp(account);
        redirectAttributes.addFlashAttribute("info", "Complete");
        return "redirect:/check-email";
    }

    @GetMapping("/check-email")
    public String checkEmail(@CurrentAccount Account account, Model model) {

        if (account != null) {
            model.addAttribute(account);
            model.addAttribute("username", account.getUsername());
            model.addAttribute("email", account.getEmail());
        }

        return "account/check-email";
    }

    //todo refactor: naming to send confirm email
    @GetMapping("/send-email")
    public String sendEmail(@CurrentAccount Account account, Model model, RedirectAttributes redirectAttributes) {

        if (account.isEmailVerified()) {
            return "redirect:/";
        }

        if (!account.canSendConfirmEmail()) {
            redirectAttributes.addFlashAttribute("error", "You can get an email once per in 15 minutes");
            return "redirect:/check-email";
        }

        accountService.sendSignUpConfirmEmail(account);
        redirectAttributes.addFlashAttribute("info", "Email has been sent");
        return "redirect:/check-email";
    }

    @GetMapping("profile/{username}")
    public String viewProfile(
            @PathVariable String username,
            @CurrentAccount Account account,
            Model model) {
        Account foundAccount = accountService.getAccount(username, account);


        model.addAttribute(foundAccount);
        model.addAttribute("isOwner", foundAccount.equals(account));
        return "account/profile";
    }

    @GetMapping("/send-email-login-link")
    public String sendEmailLoginLinkForm() {
        return "account/send-email-login-link";
    }

    //todo @Transactional need?
    @PostMapping("/send-email-login-link")
    public String sendEmailLoginLink(String email, Model model, RedirectAttributes attributes) {
        Account account = accountRepository.findByEmail(email);

        if (account == null) {
            attributes.addFlashAttribute("error", "invalid email");
            return "redirect:/send-email-login-link";
        }

        log.info("account.canSendConfirmEmail()={}", account.canSendConfirmEmail());
        if (!account.canSendConfirmEmail()) {
            attributes.addFlashAttribute("error", "You can get the email once per 15 minutes.");
            return "redirect:/send-email-login-link";
        }

        accountService.sendEmailLoginLink(account);
        attributes.addFlashAttribute("email", account.getEmail());
        attributes.addFlashAttribute("info", "Login email has been sent.");
        return "redirect:/check-email-login";
    }

    @GetMapping("/check-email-login")
    public String checkEmailLoginForm(@CurrentAccount Account account, Model model) {
        if (account != null) {
            model.addAttribute(account);
            model.addAttribute("username", account.getUsername());
            model.addAttribute("email", account.getEmail());
        }
        return "account/check-email-login";
    }

    @GetMapping("/email-login")
    public String emailLogin(String token, String email, Model model, RedirectAttributes redirectAttributes) {
        Account account = accountRepository.findByEmail(email);

        if (account == null || !account.isValidToken(token)) {
            redirectAttributes.addFlashAttribute("error", "You can not sign in");
            return "redirect:/check-email-login";
        }

        accountService.login(account);
        return "redirect:/check-email-login";
    }
*/
}
