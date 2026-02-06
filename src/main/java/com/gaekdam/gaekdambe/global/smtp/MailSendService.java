package com.gaekdam.gaekdambe.global.smtp;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class MailSendService {
  private final JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String EMAIL_SENDER;

  public MailSendService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void sendTxtEmail(MailSendRequest request) {
    SimpleMailMessage smm = new SimpleMailMessage();
    smm.setTo(request.recipient());
    smm.setFrom(EMAIL_SENDER);
    smm.setSubject(request.subject());
    smm.setText(request.content());
    try {
      mailSender.send(smm);
      System.out.println("이메일 전송 성공!");
    } catch (MailException e) {
      System.out.println("[-] 이메일 전송중에 오류가 발생하였습니다 " + e.getMessage());
      throw e;
    }
  }

  public void resetPasswordEmail(String email,String tempPassword) {
    MailSendRequest request = new MailSendRequest(
        email,
        "회원님의 비밀번호가 초기화 되었습니다.",
        "임시 비밀번호 : "+tempPassword
    );
    sendTxtEmail(request);
  }
  public void registerEmail(String email,String tempPassword) {
    MailSendRequest request = new MailSendRequest(
        email,
        "회원님의 비밀번호가 생성 되었습니다.",
        "임시 비밀번호 : "+tempPassword
    );
    sendTxtEmail(request);
  }
}
