package cn.hust.hustmall.service.impl;

import cn.hust.hustmall.common.Const;
import cn.hust.hustmall.common.ServerResponse;
import cn.hust.hustmall.common.TokenCache;
import cn.hust.hustmall.dao.UserMapper;
import cn.hust.hustmall.pojo.User;
import cn.hust.hustmall.service.IUserService;
import cn.hust.hustmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @program: hustmall
 * @author: yaopeng
 * @create: 2019-10-29 21:01
 **/
@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;


    /**
     * 登录校验
     * @param username
     * @param password
     * @return
     */
    @Override
    public ServerResponse<User> login(String username, String password) {
        if( userMapper.checkUsername(username) == 0 ){
            return  ServerResponse.createByErrorMessage("用户名不存在");
        }

        //用加密后的密码验证
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5Password);
        if(user == null){
            return ServerResponse.createByErrorMessage("密码错误，请重新输入");
        }

        //为了避免用户密码被泄露，即使加密，也要置为empty
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功",user);


    }

    /**
     * 注册用户信息
     * @param user
     * @return
     */
    @Override
    public ServerResponse<String> register(User user) {

        ServerResponse<String> response = this.checkValid(user.getUsername(), Const.USERNAME);
        if(!response.isSuccess()){
            return response;
        }


        response = this.checkValid(user.getEmail(), Const.EMAIL);
        if(!response.isSuccess()){
            return response;
        }

        //设置用户角色
        user.setRole(Const.Role.ROLE_CUSTOMER);

        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        int resultCount = userMapper.insert(user);
        if(resultCount == 0){
            return  ServerResponse.createByErrorMessage("注册失败");
        }

        return ServerResponse.createBySuccess("注册成功");

    }


    /**
     *  校验用户名和email
     * @param str
     * @param type
     * @return
     */
    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if(StringUtils.isNotBlank(type)){
            if(Const.USERNAME.equals(type)){
                if(userMapper.checkUsername(str) > 0 ){
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }

            if(Const.EMAIL.equals(type)){
                if(userMapper.checkEmail(str) >  0 ){
                    return ServerResponse.createByErrorMessage("email已存在");
                }
            }
        }else {
            return ServerResponse.createByErrorMessage("参数错误");
        }

        return ServerResponse.createBySuccess("检验成功");
    }


    /**
     * 忘记密码之得到问题
     * @param username
     * @return
     */
    @Override
    public ServerResponse<String> selectQuestion(String username) {
        ServerResponse response = this.checkValid(username,Const.USERNAME);
        if(response.isSuccess()){
            return ServerResponse.createByErrorMessage("该用户不存在");
        }

        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isBlank(question)){
            return ServerResponse.createByErrorMessage("找回密码问题为空");
        }

        return ServerResponse.createBySuccess(question);
    }


    /**
     * 忘记密码之检查答案
     * @param username
     * @param question
     * @param answer
     * @return
     */
    public ServerResponse<String> checkAnswer(String username, String question, String answer){
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if(resultCount > 0){
            //说明答案检验通过
            String forgetToken  = UUID.randomUUID().toString();
            //用guava本地缓存可以很轻便很容易的改变数据存储时间~这就比较符合忘记密码的场景了，
            // 可以根据不同的业务来设置不同的缓存策略，包括弱引用，软引用，过期时间，最大项数
            //session主要是跟踪用户状态~而且是存储在服务器端~需要手动删除已存储的数据，而且尽量小
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    /**
     * 忘记密码之重置密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    public ServerResponse<String> forgerRestPassword(String username,String passwordNew, String forgetToken) {
        //1.检查用户名
        ServerResponse<String> checkValid = this.checkValid(username, Const.USERNAME);
        if (checkValid.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        //2.检查forgetToken
        if (StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createByErrorMessage("token不能为空");
        }

        //3.查看缓存token是否已经失效
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("token无效或者过期");
        }

        //4.判断forgetToken是否与缓存token相等
        //4.1若相等，则更新数据库
        if (StringUtils.equals(token, forgetToken)) {
            String md5password = MD5Util.MD5EncodeUtf8(passwordNew);
            int count = userMapper.updatePasswordByUsername(username, md5password);
            //一定要判断是否大于0,可能会出现修改失败的情况
            if (count > 0) {
                return ServerResponse.createBySuccess("修改密码成功");
            }
        }else{
            return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
        }
            return ServerResponse.createByErrorMessage("修改密码失败");
    }


    /**
     * 登录状态下重置密码
     * @param passwordOld
     * @param passwordNew
     * @param user
     * @return
     */
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user){
        //1.检查用户和其对应的密码正不正确，为了防止横向越权和password来撞库和非本人来修改密码,
        // 要校验一下这个用户的旧密码,一定要指定是这个用户,否则通过password来撞库也能返回count >0
        int count = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(count == 0){
            return ServerResponse.createByErrorMessage("旧密码错误");
        }

        //2.更新加密后的密码
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updastResult = userMapper.updateByPrimaryKeySelective(user);
        if(updastResult > 0){
            return ServerResponse.createBySuccess("重置密码成功");
        }
        return ServerResponse.createByErrorMessage("重置密码失败");

    }

    /**
     * 更新用户信息
     * @param user
     * @return
     */

    public ServerResponse<User> updateInfomation(User user){
        //1.不能修改用户名
        //2.检查email是否存在，若email存在数据库中且不属于当前用户，则不通过
        int count = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if(count > 0){
            return ServerResponse.createByErrorMessage("该email已经存在，请重新输入");
        }
        //3.更新信息,这里最好别用Beanutils的属性copy,因为像role这类的属性是不能被更改的
        //updateByPrimaryKeySelective是根据id来更新的，所以必须传入，也可以设计一个DTO对象，前端传入DTO对象，只有四个属性段，
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setAnswer(user.getAnswer());
        updateUser.setQuestion(user.getQuestion());
        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount > 0 ){
            return ServerResponse.createBySuccess("更新个人信息成功",updateUser);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");

    }

    /**
     * 得到当前用户信息
     * @param userId
     * @return
     */
    public ServerResponse<User> getInfomation(Integer userId){

        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null){
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);

    }

}
