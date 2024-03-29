// @ts-nocheck
// This file is generated by Umi automatically
// DO NOT CHANGE IT MANUALLY!
import { Link, useLocation, useNavigate, Outlet, useAppData, useRouteData, matchRoutes } from 'umi';
import type { IRoute } from 'umi';
import React, { useMemo } from 'react';
import {
  ProLayout,
} from "D:/sqlfather-pro/sqlfather-vue/sql-father-frontend-public/node_modules/@umijs/plugins/node_modules/@ant-design/pro-layout";
import './Layout.less';
import Logo from './Logo';
import Exception from './Exception';
import { getRightRenderContent } from './rightRender';
import { useModel } from '@@/plugin-model';
import { useAccessMarkedRoutes } from '@@/plugin-access';


// 过滤出需要显示的路由, 这里的filterFn 指 不希望显示的层级
const filterRoutes = (routes: IRoute[], filterFn: (route: IRoute) => boolean) => {
  if (routes.length === 0) {
    return []
  }

  let newRoutes = []
  for (const route of routes) {
    if (filterFn(route)) {
      if (Array.isArray(route.routes)) {
        newRoutes.push(...filterRoutes(route.routes, filterFn))
      }
    } else {
      newRoutes.push(route);
      if (Array.isArray(route.routes)) {
        route.routes = filterRoutes(route.routes, filterFn);
      }
    }
  }

  return newRoutes;
}

// 格式化路由 处理因 wrapper 导致的 菜单 path 不一致
const mapRoutes = (routes: IRoute[]) => {
  if (routes.length === 0) {
    return []
  }
  return routes.map(route => {
    // 需要 copy 一份, 否则会污染原始数据
    const newRoute = {...route}
    if (route.originPath) {
      newRoute.path = route.originPath
    }

    if (Array.isArray(route.routes)) {
      newRoute.routes = mapRoutes(route.routes);
    }

    return newRoute
  })
}

export default (props: any) => {
  const location = useLocation();
  const navigate = useNavigate();
  const { clientRoutes, pluginManager } = useAppData();
  const initialInfo = (useModel && useModel('@@initialState')) || {
    initialState: undefined,
    loading: false,
    setInitialState: null,
  };
  const { initialState, loading, setInitialState } = initialInfo;
  const userConfig = {};
const formatMessage = undefined;
  const runtimeConfig = pluginManager.applyPlugins({
    key: 'layout',
    type: 'modify',
    initialValue: {
      ...initialInfo
    },
  });

  const matchedRoute = useMemo(() => matchRoutes(clientRoutes, location.pathname).pop()?.route, [location.pathname]);
  const newRoutes = filterRoutes(clientRoutes.filter(route => route.id === 'ant-design-pro-layout'), (route) => {
    return (!!route.isLayout && route.id !== 'ant-design-pro-layout') || !!route.isWrapper;
  })
  const [route] = useAccessMarkedRoutes(mapRoutes(newRoutes));

  return (
    <ProLayout
      route={route}
      location={location}
      title={userConfig.title || 'plugin-layout'}
      navTheme="dark"
      siderWidth={256}
      onMenuHeaderClick={(e) => {
        e.stopPropagation();
        e.preventDefault();
        navigate('/');
      }}
      formatMessage={userConfig.formatMessage || formatMessage}
      menu={{ locale: userConfig.locale }}
      logo={Logo}
      menuItemRender={(menuItemProps, defaultDom) => {
        if (menuItemProps.isUrl || menuItemProps.children) {
          return defaultDom;
        }
        if (menuItemProps.path && location.pathname !== menuItemProps.path) {
          return (
            // handle wildcard route path, for example /slave/* from qiankun
            <Link to={menuItemProps.path.replace('/*', '')} target={menuItemProps.target}>
              {defaultDom}
            </Link>
          );
        }
        return defaultDom;
      }}
      disableContentMargin
      fixSiderbar
      fixedHeader
      {...runtimeConfig}
      rightContentRender={
        runtimeConfig.rightContentRender !== false &&
        ((layoutProps) => {
          const dom = getRightRenderContent({
            runtimeConfig,
            loading,
            initialState,
            setInitialState,
          });
          if (runtimeConfig.rightContentRender) {
            return runtimeConfig.rightContentRender(layoutProps, dom, {
              // BREAK CHANGE userConfig > runtimeConfig
              userConfig,
              runtimeConfig,
              loading,
              initialState,
              setInitialState,
            });
          }
          return dom;
        })
      }
    >
      <Exception
        route={matchedRoute}
        notFound={runtimeConfig.notFound}
        noAccessible={runtimeConfig.noAccessible}
      >
        {runtimeConfig.childrenRender
          ? runtimeConfig.childrenRender(<Outlet />, props)
          : <Outlet />
        }
      </Exception>
    </ProLayout>
  );
}
