package com.gaekdam.gaekdambe.global.smtp;

public record MailSendRequest (
    String recipient, //받는 이메일
    String subject,
    String content

){

}
