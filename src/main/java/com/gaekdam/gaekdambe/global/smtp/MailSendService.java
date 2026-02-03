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
    smm.setTo(request.recipient());               // 받는 사람 이메일
    smm.setFrom(EMAIL_SENDER);                              // [해당 부분 추가!!!] 보내는 사람 추가
    smm.setSubject(request.subject());            // 이메일 제목
    smm.setText(request.content());               // 이메일 내용
    try {
      mailSender.send(smm);                   // 메일 보내기
      System.out.println("이메일 전송 성공!");
    } catch (MailException e) {
      System.out.println("[-] 이메일 전송중에 오류가 발생하였습니다 " + e.getMessage());
      throw e;
    }
  }

  public void resetPasswordEmail(String email,String tempPassword) {
    MailSendRequest request = new MailSendRequest(
        email,
        "회원님의 비밀번호가 갱신 되었습니다.",
        "임시 비밀번호 : "+tempPassword
    );
    sendTxtEmail(request);
  }

}
