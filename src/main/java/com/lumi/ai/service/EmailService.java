package com.lumi.ai.service;

import com.resend.Resend;
import com.resend.services.emails.model.SendEmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final Resend resend;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public EmailService(@Value("${app.resend.api-key}") String apiKey) {
        this.resend = new Resend(apiKey);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject(subject)
                    .html(htmlBody)
                    .build();

            resend.emails().send(sendEmailRequest);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar e-mail via Resend para " + to + ": " + e.getMessage(), e);
        }
    }

    @Async("emailExecutor")
    public void sendPasswordResetEmail(String to, String resetCode) {
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
                                        <img src="%s/logo.png" alt="LumiAI" style="width:120px;height:auto;" />
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
    }

    @Async("emailExecutor")
    public void sendWelcomeEmail(String to, String name, String temporaryPassword) {
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
    }

    @Async("emailExecutor")
    public void sendOtpEmail(String to, String otp) {
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
    }
}
