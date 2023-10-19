import {
  BugOutlined,
  GithubOutlined,
  SketchOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { DefaultFooter } from '@ant-design/pro-components';
import React from 'react';
import './index.less';

/**
 * 全局 Footer
 *
 * @author https://github.com/LMS2000
 */
const GlobalFooter: React.FC = () => {
  const currentYear = new Date().getFullYear();

  return (
    <DefaultFooter
      className="default-footer"
      copyright={`${currentYear} 程序员LMS`}
      links={[
        {
          key: 'master',
          title: (
            <>
              <UserOutlined /> 站长：LMS
            </>
          ),
          href: 'http://www.luomosan.top/',
          blankTarget: true,
        },
        {
          key: 'github',
          title: (
            <>
              <GithubOutlined /> 代码仓库
            </>
          ),
          href: 'https://github.com/LMS2000/lms2000-sql-father',
          blankTarget: true,
        } 
      ]}
    />
  );
};

export default GlobalFooter;
