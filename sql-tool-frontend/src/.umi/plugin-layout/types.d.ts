// This file is generated by Umi automatically
// DO NOT CHANGE IT MANUALLY!
import type { ProLayoutProps } from "D:/sqlfather-pro/sqlfather-vue/sql-father-frontend-public/node_modules/@umijs/plugins/node_modules/@ant-design/pro-layout";
    import type InitialStateType from '@@/plugin-initialState/@@initialState';
           type InitDataType = ReturnType<typeof InitialStateType>;
        

    export type RunTimeLayoutConfig = (
      initData: InitDataType,
    ) => ProLayoutProps & {
      childrenRender?: (dom: JSX.Element, props: ProLayoutProps) => React.ReactNode,
      noAccessible?: JSX.Element,
      notFound?: JSX.Element,
    };
