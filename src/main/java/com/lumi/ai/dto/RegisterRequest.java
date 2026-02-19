package com.lumi.ai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "O nome é obrigatório.")
    private String name;

    @Email(message = "E-mail inválido. Verifique o formato do e-mail.")
    @NotBlank(message = "O e-mail é obrigatório.")
    private String email;

    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
    private String password;

    @NotBlank(message = "O campo 'role' é obrigatório.")
    @Pattern(regexp = "ADMIN|USER", message = "Tipo do usuário inválido. Deve ser ADMIN ou USER.")
    private String role;

    @NotBlank(message = "O telefone é obrigatório.")
    private String phone;

    @NotBlank(message = "O CPF é obrigatório.")
    private String cpf;
}
