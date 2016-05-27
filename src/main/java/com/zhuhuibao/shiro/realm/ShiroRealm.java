package com.zhuhuibao.shiro.realm;

import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.zhuhuibao.mybatis.memberReg.entity.LoginMember;
import com.zhuhuibao.mybatis.memberReg.entity.Member;
import com.zhuhuibao.mybatis.memberReg.service.MemberRegService;

import java.io.Serializable;


/**
 * @author jianglz
 */
public class ShiroRealm extends AuthorizingRealm {
    private static final Logger log = LoggerFactory.getLogger(ShiroRealm.class);

    @Autowired
    private MemberRegService memberRegService;

    /**
     * 授权查询回调函数, 进行鉴权但缓存中无用户的授权信息时调用.
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(
            PrincipalCollection principals) {
    	ShiroUser member = (ShiroUser)principals.fromRealm(getName()).iterator().next();
    	if(null != member){
    		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
    		info.addRole(member.getIdentifyname()+"."+member.getRolename());
    		return info;
    	}
        return null;
    }

    /**
     * 认证回调函数,登录时调用.
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(
            AuthenticationToken token) throws AuthenticationException {
        log.info("登录认证");
        String loginname = (String) token.getPrincipal();
        /*Member member = memberRegService.findMemberByAccount(loginname);
        if(member != null){
            if (0 == member.getStatus() || 2 == member.getStatus()) {
                throw new LockedAccountException(); // 帐号不正常状态
            }
        }  else{
            throw new UnknownAccountException();//  用户名不存在
        }*/
        LoginMember loginMember = memberRegService.getLoginMemberByAccount(loginname);
        if(loginMember != null){
            if (0 == loginMember.getStatus() || 2 == loginMember.getStatus()) {
                throw new LockedAccountException(); // 帐号不正常状态
            }
        }  else{
            throw new UnknownAccountException();//  用户名不存在
        }
        
        // 交给AuthenticatingRealm使用CredentialsMatcher进行密码匹配
        return new SimpleAuthenticationInfo(
                new ShiroUser(loginMember.getId(), loginMember.getAccount(),loginMember.getStatus(),
                		loginMember.getIdentifyname(),loginMember.getRolename()), // 用户
                loginMember.getPassword(), // 密码
//                ByteSource.Util.bytes("123"),
                getName() // realm name
        );
    }


    /**
     * 更新用户授权信息缓存.
     */
    public void clearCachedAuthorizationInfo(Object principal) {
        SimplePrincipalCollection principals = new SimplePrincipalCollection(
                principal, getName());
        clearCachedAuthorizationInfo(principals);
    }


    /**
     * 自定义Authentication对象，使得Subject除了携带用户的登录名外还可以携带更多信息.
     */
    public static class ShiroUser implements Serializable {
        private static final long serialVersionUID = -1373760761780840081L;
        private Long id;
        private String account;
        private int status;
        private String identifyname;
        private String rolename;

        public ShiroUser(Long id, String account, int status, String identifyname, String rolename) {
            this.id = id;
            this.account = account;
            this.status = status;
            this.identifyname = identifyname;
            this.rolename = rolename;
        }
        
		public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
        
        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }
        
        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

		public String getIdentifyname() {
			return identifyname;
		}

		public void setIdentifyname(String identifyname) {
			this.identifyname = identifyname;
		}

		public String getRolename() {
			return rolename;
		}

		public void setRolename(String rolename) {
			this.rolename = rolename;
		}

        /**
         * 重载equals,只计算id+account;
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ShiroUser other = (ShiroUser) obj;

            if (id == null || account == null) {
                return false;
            } else if (id.equals(other.id)
                    && account.equals(other.account))
                return true;
            return false;
        }

    }

}
