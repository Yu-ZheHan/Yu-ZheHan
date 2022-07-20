package com.yzh.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yzh.reggie.common.R;
import com.yzh.reggie.entity.Employee;
import com.yzh.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee>login(HttpServletRequest request, @RequestBody Employee employee){
        //1.md5加密处理
        String password=employee.getPassword();
        password= DigestUtils.md5DigestAsHex(password.getBytes());

        //2.查数据库
        LambdaQueryWrapper<Employee> querWrapper=new LambdaQueryWrapper<>();
        querWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp=employeeService.getOne(querWrapper);

        //3.返回登陆失败结果
        if(emp==null){
            return  R.error("登录失败");
        }
        //4.密码比对

        if(!emp.getPassword().equals(password)){
            return  R.error("登陆失败");
        }

        //查看员工状态
        if(emp.getStatus()==0){
            return  R.error("账号禁用");
        }

        //6.登陆成功
        request.getSession().setAttribute("employee",emp.getId());
        return  R.success(emp);

    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("logout")
    public R<String>logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工，员工信息:{}",employee.toString());

        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    /**
     * 员工信息查询分页
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page>page(int page ,int pageSize,String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);

        //构建分页构造器
        Page pageInfo=new Page(page,pageSize);
        //构造器条件构造器
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);

    }

    /**
     * 根据id修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public R<String>update(HttpServletRequest request,@RequestBody Employee employee){
        Long empId=(Long)request.getSession().getAttribute("employee");
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);
        employeeService.updateById(employee);

        return  R.success("员工信息修改成功");
    }

    /**
     *根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工");
        Employee employee=employeeService.getById(id);
        if(employee!=null){
        return R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
    }

}