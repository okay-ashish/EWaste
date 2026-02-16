package com.E_waste.E_Waste.Service;

import com.E_waste.E_Waste.dto.AuthRequest;
import com.E_waste.E_Waste.dto.UserProfileResponse;
import com.E_waste.E_Waste.Entity.User;
import com.E_waste.E_Waste.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return mapToResponse(user);
    }

    public UserProfileResponse updateProfile(String email, User request) {
        User user = userRepository.findByEmail(email).orElseThrow();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setPincode(request.getPincode());
        user.setIsVerified(request.getIsVerified());

        userRepository.save(user);
        return mapToResponse(user);
    }

    public UserProfileResponse updateProfile(String email, AuthRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setPincode(request.getPincode());


        userRepository.save(user);
        return mapToResponse(user);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    private UserProfileResponse mapToResponse(User user) {
        return new UserProfileResponse(

                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getAddress(),
                user.getPincode(),
                user.getIsVerified()
        );
    }
}
