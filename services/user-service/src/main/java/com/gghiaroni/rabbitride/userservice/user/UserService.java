package com.gghiaroni.rabbitride.userservice.user;

import com.gghiaroni.rabbitride.userservice.user.dto.RegisterRequest;
import com.gghiaroni.rabbitride.userservice.user.dto.RegisterResponse;
import com.gghiaroni.rabbitride.userservice.user.exception.CpfAlreadyExistsException;
import com.gghiaroni.rabbitride.userservice.user.exception.EmailAlreadyExistsException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest registerRequest) {
        String cpfNormalizado = registerRequest.cpf().replaceAll("\\D", "");

        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new EmailAlreadyExistsException(registerRequest.email());
        }
        if (userRepository.existsByCpf(cpfNormalizado)) {
            throw new CpfAlreadyExistsException(cpfNormalizado);
        }

        User user = User.builder()
            .nome(registerRequest.nome())
            .email(registerRequest.email())
            .senha(passwordEncoder.encode(registerRequest.senha()))
            .cpf(cpfNormalizado)
            .build();

        return RegisterResponse.from(userRepository.save(user));
    }


}
