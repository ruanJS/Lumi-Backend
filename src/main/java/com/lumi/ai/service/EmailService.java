package com.lumi.ai.service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final Resend resend;

    @Value("${app.email.from:onboarding@resend.dev}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public EmailService(
            @Autowired(required = false) JavaMailSender mailSender,
            @Value("${app.resend.api-key:}") String apiKey) {
        this.mailSender = mailSender;
        
        // Prioritize Resend if a valid API key is present
        if (apiKey != null && apiKey.startsWith("re_")) {
            this.resend = new Resend(apiKey);
            logger.info("EmailService: Resend API inicializada.");
        } else {
            this.resend = null;
            if (mailSender != null) {
                logger.info("EmailService: Usando SMTP (Gmail) como provedor local.");
            } else {
                logger.warn("EmailService: Nenhum provedor de e-mail (Resend ou SMTP) configurado.");
            }
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        if (resend != null) {
            sendViaResend(to, subject, htmlBody);
        } else if (mailSender != null) {
            sendViaSmtp(to, subject, htmlBody);
        } else {
            logger.warn("Tentativa de envio de e-mail ignorada (Provedor não configurado) para: {}", to);
        }
    }

    private void sendViaResend(String to, String subject, String htmlBody) {
        try {
            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject(subject)
                    .html(htmlBody)
                    .build();

            resend.emails().send(options);
            logger.info("E-mail enviado via Resend para: {}", to);
        } catch (Exception e) {
            logger.error("EmailService: Falha ao enviar e-mail via Resend para {}: {}", to, e.getMessage());
        }
    }

    private void sendViaSmtp(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            logger.info("E-mail enviado via SMTP para: {}", to);
        } catch (MessagingException e) {
            logger.error("EmailService: Falha ao enviar e-mail via SMTP para {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String resetCode) {
        try {
            String subject = "Código de redefinição de senha - LumiAI";
            String htmlBody = String.format("""
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;background:#f4f6fb;font-family:Arial, sans-serif;">
                    <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr><td align="center" style="padding:40px 20px;">
                            <table width="600" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 10px 25px rgba(0,0,0,0.08);">
                                <tr>
                                    <td style="background:linear-gradient(135deg,#4f46e5,#7c3aed);padding:32px;text-align:center;">
                                        <img src="%s/lumi-logo.png" alt="LumiAI" style="width:120px;height:auto;" />
                                        <h1 style="color:#ffffff;margin:20px 0 0;font-size:24px;">LumiAI</h1>
                                        <p style="color:#e0e7ff;margin:8px 0 0;font-size:14px;">Gestão financeira inteligente</p>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding:40px;">
                                        <h2 style="margin-top:0;color:#1f2937;">Redefinição de Senha</h2>
                                        <p style="color:#4b5563;font-size:16px;">Recebemos uma solicitação para redefinir sua senha.</p>
                                        <div style="margin:32px 0;text-align:center;">
                                            <div style="display:inline-block;padding:20px 40px;background:#eef2ff;border-radius:12px;border:2px solid #6366f1;">
                                                <span style="font-size:32px;font-weight:bold;letter-spacing:6px;color:#4f46e5;font-family:monospace;">%s</span>
                                            </div>
                                        </div>
                                        <p style="color:#6b7280;font-size:14px;">Este código é válido por 10 minutos.</p>
                                        <p style="color:#6b7280;font-size:14px;">Se você não solicitou essa redefinição, ignore este e-mail.</p>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="background:#111827;padding:20px;text-align:center;">
                                        <p style="color:#9ca3af;font-size:12px;margin:0;">© 2026 LumiAI. Todos os direitos reservados.</p>
                                    </td>
                                </tr>
                            </table>
                        </td></tr>
                    </table>
                </body>
                </html>
                """, frontendUrl, resetCode);

            sendHtmlEmail(to, subject, htmlBody);
        } catch (Exception e) {
            logger.error("EmailService: Erro ao enviar e-mail de redefinição para {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendWelcomeEmail(String to, String name, String temporaryPassword) {
        try {
            String subject = "Bem-vindo à LumiAI";
            String htmlBody = String.format("""
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;background:#f4f6fb;font-family:Arial, sans-serif;">
                    <table width="100%%" cellpadding="0" cellspacing="0">
                        <tr><td align="center" style="padding:40px 20px;">
                            <table width="600" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 10px 25px rgba(0,0,0,0.08);">
                                <tr>
                                    <td style="background:linear-gradient(135deg,#4f46e5,#7c3aed);padding:32px;text-align:center;">
                                        <img src="%s/lumi-logo.png" alt="LumiAI" style="width:120px;height:auto;" />
                                        <h1 style="color:#ffffff;margin:20px 0 0;font-size:24px;">LumiAI</h1>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding:40px;">
                                        <h2 style="margin-top:0;color:#1f2937;">Bem-vindo, %s</h2>
                                        <p style="color:#4b5563;font-size:16px;">Sua conta foi criada com sucesso.</p>
                                        <div style="margin:24px 0;padding:20px;background:#f9fafb;border-radius:12px;border-left:4px solid #6366f1;">
                                            <p style="margin:0;font-size:14px;color:#6b7280;">Email</p>
                                            <p style="margin:0 0 12px;font-weight:bold;color:#111827;">%s</p>
                                            <p style="margin:0;font-size:14px;color:#6b7280;">Senha provisória</p>
                                            <p style="margin:0;font-weight:bold;color:#dc2626;font-family:monospace;">%s</p>
                                        </div>
                                        <div style="text-align:center;margin-top:32px;">
                                            <a href="%s" style="display:inline-block;padding:14px 32px;background:linear-gradient(135deg,#4f46e5,#7c3aed);color:#ffffff;text-decoration:none;border-radius:8px;font-weight:600;">Acessar Plataforma</a>
                                        </div>
                                        <p style="margin-top:24px;color:#6b7280;font-size:14px;">Recomendamos alterar sua senha no primeiro acesso.</p>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="background:#111827;padding:20px;text-align:center;">
                                        <p style="color:#9ca3af;font-size:12px;margin:0;">© 2026 LumiAI. Todos os direitos reservados.</p>
                                    </td>
                                </tr>
                            </table>
                        </td></tr>
                    </table>
                </body>
                </html>
                """, frontendUrl, name, to, temporaryPassword, frontendUrl);

            sendHtmlEmail(to, subject, htmlBody);
        } catch (Exception e) {
            logger.error("EmailService: Erro ao enviar e-mail de boas-vindas para {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendOtpEmail(String to, String otp) {
        try {
            String subject = "Seu código de verificação administrativo - LumiAI";
            String htmlBody = String.format("""
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eaeaea; border-radius: 10px;">
                    <div style="background:linear-gradient(135deg,#4f46e5,#7c3aed);padding:24px;text-align:center;border-radius:8px 8px 0 0;margin:-20px -20px 20px -20px;">
                        <img src="%s/lumi-logo.png" alt="LumiAI" style="width:100px;height:auto;" />
                        <h2 style="color: #ffffff; margin: 10px 0 0;">LumiAI Admin</h2>
                    </div>
                    <p>Olá,</p>
                    <p>Foi solicitado um acesso administrativo à sua conta. Use o código de verificação abaixo para completar o login seguro (2FA).</p>
                    <div style="background-color: #f3f4f6; padding: 15px; text-align: center; border-radius: 8px; margin: 25px 0;">
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #111827;">%s</span>
                    </div>
                    <p style="color: #ef4444; font-size: 14px;"><strong>Atenção:</strong> Este código expira em 5 minutos e só pode ser utilizado uma vez.</p>
                    <hr style="border: none; border-top: 1px solid #eaeaea; margin: 30px 0;" />
                    <p style="font-size: 12px; color: #6b7280;">Se você não solicitou este acesso, ignore este email ou contate o suporte imediatamente.</p>
                </div>
                """, frontendUrl, otp);

            sendHtmlEmail(to, subject, htmlBody);
        } catch (Exception e) {
            logger.error("EmailService: Erro ao enviar e-mail OTP para {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendWaitlistEmail(String to, String name) {
        try {
            String firstName = (name != null && name.contains(" ")) ? name.split(" ")[0] : name;
            String subject = "Bem-vindo à nossa lista exclusiva - LumiAI 💎";
            
            String htmlBody = String.format("""
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head><meta charset="UTF-8"></head>
                <body style="margin:0;padding:0;background-color:#0f172a;font-family:'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;color:#ffffff;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#0f172a;padding:40px 20px;">
                        <tr><td align="center">
                            <table width="600" style="background:linear-gradient(180deg, #1e293b 0%%, #0f172a 100%%);border-radius:24px;overflow:hidden;border:1px solid rgba(255,255,255,0.1);">
                                <tr>
                                    <td style="padding:48px 40px;text-align:center;">
                                        <div style="margin-bottom:32px;">
                                            <img src="%s/lumi-logo.png" alt="LumiAI" style="width:80px;height:auto;" />
                                        </div>
                                        <h1 style="font-size:32px;font-weight:bold;margin:0 0 16px;background:linear-gradient(135deg,#818cf8,#c084fc);-webkit-background-clip:text;color:#818cf8;">Você está na lista!</h1>
                                        <p style="font-size:18px;line-height:1.6;color:#94a3b8;margin:0;">Olá, %s. Recebemos seu interesse na <strong>LumiAI</strong>.</p>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding:0 40px 40px;">
                                        <div style="background:rgba(255,255,255,0.03);border-radius:20px;padding:32px;border:1px solid rgba(255,255,255,0.05);">
                                            <h2 style="font-size:20px;margin:0 0 12px;color:#ffffff;">O que acontece agora?</h2>
                                            <p style="font-size:16px;line-height:1.6;color:#94a3b8;margin:0 0 24px;">
                                                Estamos finalizando os últimos detalhes da nossa inteligência artificial para garantir que você tenha a melhor experiência em gestão financeira.
                                            </p>
                                            <ul style="padding:0;margin:0;list-style:none;">
                                                <li style="margin-bottom:12px;display:flex;align-items:center;">
                                                    <span style="color:#818cf8;margin-right:10px;">✨</span>
                                                    <span style="color:#cbd5e1;">Acesso antecipado à plataforma</span>
                                                </li>
                                                <li style="margin-bottom:12px;display:flex;align-items:center;">
                                                    <span style="color:#818cf8;margin-right:10px;">💎</span>
                                                    <span style="color:#cbd5e1;">Condições especiais de lançamento</span>
                                                </li>
                                                <li style="display:flex;align-items:center;">
                                                    <span style="color:#818cf8;margin-right:10px;">🚀</span>
                                                    <span style="color:#cbd5e1;">Novidades exclusivas no seu e-mail</span>
                                                </li>
                                            </ul>
                                        </div>
                                        <div style="text-align:center;margin-top:40px;">
                                            <p style="font-size:14px;color:#64748b;margin-bottom:24px;">Siga-nos para acompanhar a jornada:</p>
                                            <a href="https://instagram.com/lumiai" style="display:inline-block;padding:12px 24px;background:rgba(255,255,255,0.05);color:#ffffff;text-decoration:none;border-radius:12px;font-weight:600;margin:0 8px;border:1px solid rgba(255,255,255,0.1);">Instagram</a>
                                            <a href="%s" style="display:inline-block;padding:12px 24px;background:linear-gradient(135deg,#4f46e5,#7c3aed);color:#ffffff;text-decoration:none;border-radius:12px;font-weight:600;margin:0 8px;box-shadow:0 10px 15px -3px rgba(79, 70, 229, 0.4);">Visite nosso Site</a>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="background:rgba(0,0,0,0.2);padding:24px;text-align:center;border-top:1px solid rgba(255,255,255,0.05);">
                                        <p style="font-size:12px;color:#475569;margin:0;">LumiAI - Inteligência Artificial para sua Empresa</p>
                                        <p style="font-size:11px;color:#334155;margin:8px 0 0;">© 2026 Todos os direitos reservados.</p>
                                    </td>
                                </tr>
                            </table>
                        </td></tr>
                    </table>
                </body>
                </html>
                """, frontendUrl, firstName, frontendUrl);

            sendHtmlEmail(to, subject, htmlBody);
        } catch (Exception e) {
            logger.error("EmailService: Erro ao enviar e-mail de lista de espera para {}: {}", to, e.getMessage());
        }
    }
}
