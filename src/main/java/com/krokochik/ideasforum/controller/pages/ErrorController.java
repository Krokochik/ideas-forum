package com.krokochik.ideasforum.controller.pages;

//@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {
//
//    @GetMapping("/error")
//    public String handleError(HttpServletRequest request, Model model) {
//        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
//
//        if (status != null) {
//            int statusCode = Integer.parseInt(status.toString());
//
//            if (statusCode == 403)
//                return "redirect:/email-validity-confirmation";
//
//            model.addAttribute("code", statusCode + "");
//            model.addAttribute("message", "Error " + statusCode + " : " +  HttpStatus.valueOf(statusCode).getReasonPhrase());
//        }
//        return "error";
//    }
}
