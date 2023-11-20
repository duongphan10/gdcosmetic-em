package com.vn.em.service.impl;

import com.vn.em.constant.*;
import com.vn.em.domain.dto.pagination.PaginationFullRequestDto;
import com.vn.em.domain.dto.pagination.PaginationResponseDto;
import com.vn.em.domain.dto.pagination.PagingMeta;
import com.vn.em.domain.dto.request.ChangeAvatarRequestDto;
import com.vn.em.domain.dto.request.ChangePasswordRequestDto;
import com.vn.em.domain.dto.request.UserCreateDto;
import com.vn.em.domain.dto.request.UserUpdateDto;
import com.vn.em.domain.dto.response.CommonResponseDto;
import com.vn.em.domain.dto.response.UserDto;
import com.vn.em.domain.entity.Employee;
import com.vn.em.domain.entity.Role;
import com.vn.em.domain.entity.User;
import com.vn.em.domain.mapper.UserMapper;
import com.vn.em.exception.AlreadyExistException;
import com.vn.em.exception.NotFoundException;
import com.vn.em.repository.DepartmentRepository;
import com.vn.em.repository.EmployeeRepository;
import com.vn.em.repository.RoleRepository;
import com.vn.em.repository.UserRepository;
import com.vn.em.service.UserService;
import com.vn.em.util.FileUtil;
import com.vn.em.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{id.toString()}));
        return userMapper.mapUserToUserDto(user);
    }

    @Override
    public UserDto getCurrentUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{id.toString()}));
        if (!user.isEnabled()) {
            throw new DisabledException(ErrorMessage.ERR_EXCEPTION_DISABLED);
        }
        return userMapper.mapUserToUserDto(user);
    }

    @Override
    public PaginationResponseDto<UserDto> getAll(Integer departmentId, Boolean enabled, PaginationFullRequestDto paginationFullRequestDto) {
        if (departmentId != null) {
            departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new NotFoundException(ErrorMessage.Department.ERR_NOT_FOUND_ID, new String[]{departmentId.toString()}));
        }
        //Pagination
        Pageable pageable = PaginationUtil.buildPageable(paginationFullRequestDto, SortByDataConstant.USER);

        //Create Output
        Page<User> userPage = userRepository.getAll(paginationFullRequestDto.getKeyword(), departmentId, enabled, pageable);
        PagingMeta meta = PaginationUtil
                .buildPagingMeta(paginationFullRequestDto, SortByDataConstant.USER, userPage);
        List<UserDto> userDtos = userMapper.mapUsersToUserDtos(userPage.getContent());

        return new PaginationResponseDto<>(meta, userDtos);
    }

    @Override
    public UserDto createUser(UserCreateDto userCreateDto) {
        Employee employee = employeeRepository.findById(userCreateDto.getEmployeeId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Employee.ERR_NOT_FOUND_ID, new String[]{userCreateDto.getEmployeeId().toString()}));
        Role role = roleRepository.findById(userCreateDto.getRoleId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Role.ERR_NOT_FOUND_ID, new String[]{userCreateDto.getRoleId().toString()}));
        if (userRepository.existsByEmployee(employee)) {
            throw new AlreadyExistException(ErrorMessage.Employee.ERR_ALREADY_EXIST_USER);
        }

        if (userRepository.existsByUsername(userCreateDto.getUsername())) {
            throw new AlreadyExistException(ErrorMessage.User.ERR_ALREADY_EXIST,
                    new String[]{"username: " + userCreateDto.getUsername()});
        }
        User user = userMapper.mapUserCreateToUser(userCreateDto);
        user.setPassword(passwordEncoder.encode(userCreateDto.getPassword()));
        user.setEmployee(employee);
        user.setRole(role);
        user.setEnabled(true);
        return userMapper.mapUserToUserDto(userRepository.save(user));
    }

    @Override
    public UserDto updateUserById(Integer id, UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{id.toString()}));
        Role role = roleRepository.findById(userUpdateDto.getRoleId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Role.ERR_NOT_FOUND_ID, new String[]{userUpdateDto.getRoleId().toString()}));
        if (userRepository.existsByUsername(userUpdateDto.getUsername())) {
            throw new AlreadyExistException(ErrorMessage.User.ERR_ALREADY_EXIST,
                    new String[]{"username: " + userUpdateDto.getUsername()});
        }
        userMapper.updateUser(user, userUpdateDto);
        user.setRole(role);
        return userMapper.mapUserToUserDto(userRepository.save(user));
    }

    @Override
    public UserDto changeAvatar(Integer id, ChangeAvatarRequestDto changeAvatarRequestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{id.toString()}));
        if (user.getAvatar() != null) {
            FileUtil.deleteFile(user.getAvatar());
        }
        user.setAvatar(FileUtil.saveFile(CommonConstant.UPLOAD_PATH_IMAGE, changeAvatarRequestDto.getAvatar()));
        return userMapper.mapUserToUserDto(userRepository.save(user));
    }

    @Override
    public CommonResponseDto changePassword(Integer id, ChangePasswordRequestDto changePasswordRequestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{id.toString()}));
        if (!passwordEncoder.matches(changePasswordRequestDto.getCurrentPassword(), user.getPassword())) {
            return new CommonResponseDto(false, MessageConstant.CURRENT_PASSWORD_INCORRECT);
        }

        if (changePasswordRequestDto.getCurrentPassword().equals(changePasswordRequestDto.getNewPassword())) {
            return new CommonResponseDto(false, MessageConstant.SAME_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequestDto.getNewPassword()));
        userRepository.save(user);

        return new CommonResponseDto(true, MessageConstant.CHANGE_PASSWORD_SUCCESSFULLY);
    }

    @Override
    public CommonResponseDto deleteUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{id.toString()}));
        userRepository.delete(user);
        return new CommonResponseDto(true, MessageConstant.DELETE_USER_SUCCESSFULLY);
    }

}
