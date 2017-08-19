package com.yanhuahealth.healthlauncher.sys;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.yanhuahealth.healthlauncher.api.ApiBaseParam;
import com.yanhuahealth.healthlauncher.api.ApiConst;
import com.yanhuahealth.healthlauncher.api.ApiResponseResult;
import com.yanhuahealth.healthlauncher.api.HealthApi;
import com.yanhuahealth.healthlauncher.common.LauncherConst;
import com.yanhuahealth.healthlauncher.common.YHLog;
import com.yanhuahealth.healthlauncher.model.sys.BroadEvent;
import com.yanhuahealth.healthlauncher.sys.task.LauncherTaskItem;
import com.yanhuahealth.healthlauncher.sys.task.LauncherTaskPriority;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 中间的业务层调度
 */
public class MainService {

    public String tag() {
        return MainService.class.getName();
    }

    public ApiBaseParam apiBaseParam;

    // singleton
    private static volatile MainService instance;

    public static MainService getInstance() {
        if (instance == null) {
            synchronized (MainService.class) {
                if (instance == null) {
                    instance = new MainService();
                }
            }

        }

        return instance;
    }

    public void init() {
        apiBaseParam = new ApiBaseParam();
        apiBaseParam.setAppId(LauncherConst.APP_ID);
        apiBaseParam.setAppToken(LauncherConst.APP_KEY);
        apiBaseParam.setUserId(100);
    }

    /**
     * 管理所有实现 ITaskCallback 接口的实例，key 为对应的 ITaskCallback 实例名
     */
    private Map<String, ITaskCallback> regedServiceCallbackComponent = new HashMap<>();

    /**
     * 注册 UI 组件，由各个 UI 组件在启动时自动 register
     */
    public void regServiceCallbackComponent(String name, ITaskCallback component) {
        regedServiceCallbackComponent.put(name, component);
    }

    /**
     * 解注册 UI 组件
     */
    public void unregServiceCallbackComponent(String name) {
        regedServiceCallbackComponent.remove(name);
    }

    /**
     * 获取 实现 IServiceCallbackTask 的组件
     */
    public ITaskCallback getServiceCallbackComponent(String name) {
        if (name == null) {
            return null;
        }
        return regedServiceCallbackComponent.get(name);
    }

    // 维护所有的关注指定事件的 事件处理器
    private Map<Integer, List<IEventHandler>> regEventHandlers = new HashMap<>();
    private Lock lockRegEventHandlers = new ReentrantLock();

    // 事件处理器注册
    public boolean regEventHandler(int eventType, IEventHandler handler) {

        YHLog.d(tag(), "regEventHandler - eventType: " + eventType);

        if (handler == null) {
            return false;
        }

        lockRegEventHandlers.lock();
        try {
            List<IEventHandler> lstHandler = regEventHandlers.get(eventType);
            if (lstHandler == null) {
                lstHandler = new CopyOnWriteArrayList<>();
                regEventHandlers.put(eventType, lstHandler);
            }

            for (IEventHandler existsHandler : lstHandler) {
                if (existsHandler == handler) {
                    return true;
                }
            }

            lstHandler.add(handler);
        } finally {
            lockRegEventHandlers.unlock();
        }

        return true;
    }

    // 事件处理器撤销注册
    public boolean unregEventHandler(int eventType, IEventHandler handler) {

        YHLog.d(tag(), "unregEventHandler - eventType: " + eventType);
        if (handler == null) {
            return false;
        }

        lockRegEventHandlers.lock();
        try {
            List<IEventHandler> lstHandler = regEventHandlers.get(eventType);
            if (lstHandler == null || lstHandler.size() > 0) {
                YHLog.w(tag(), "unregEventHandler - no event handler of eventType: " + eventType);
                return false;
            }

            return lstHandler.remove(handler);
        } finally {
            lockRegEventHandlers.unlock();
        }
    }

    /**
     * 由外部应用通知注册的服务组件相应的广播事件
     * 注：只能在 UI 层调用
     */
    public void sendBroadEvent(BroadEvent event) {

        if (event == null) {
            YHLog.w(tag(), "sendBroadEvent - event is null");
            return;
        }

        lockRegEventHandlers.lock();
        try {
            if (regEventHandlers != null && regEventHandlers.size() > 0
                    && regEventHandlers.containsKey(event.eventType)) {
                List<IEventHandler> handlers = regEventHandlers.get(event.eventType);
                for (IEventHandler eventHandler : handlers) {
                    if (eventHandler != null) {
                        eventHandler.notify(event);
                    }
                }
            }
        } finally {
            lockRegEventHandlers.unlock();
        }
    }

    // 需要回调的组件名称
    public static final String MSG_PARAM_COMP = "comp";

    // 任务信息
    public static final String MSG_PARAM_TASK = "task";

    // API 响应结果的参数名
    public static final String MSG_PARAM_API_RESP = "api-resp-result";

    /**
     * 维护任务类型标识与待执行的 API 名称之间的映射关系
     */
    private static Map<Integer, String> taskTypeMethod = new HashMap<>();

    static {
        // 初始化 任务类型 与 API 方法名 的映射关系
        // 后续 API 方法名的更新也需要同步更新这里的映射关系

        /****       资讯     ****/
        taskTypeMethod.put(TaskType.GET_NEWS, "getNews");

        /****       电子书     ****/
        taskTypeMethod.put(TaskType.GET_EBOOKS, "getEbooks");

        /****       语音频道     ****/
        taskTypeMethod.put(TaskType.GET_VOICES, "getVoices");
    }

    /**
     * 处理需要递送数据至 UI 组件的处理器
     * 该 handler 是和业务相关的逻辑处理
     * 将不同业务的消息通过 回调 方式递送至对应的 component
     * <p/>
     * Message 结构:
     * <p/>
     * - what: 任务类型标识
     * - obj:  调用 API 后返回的结果对象，ApiResponseResult
     * - data: 为一个 bundle
     * + MSG_PARAM_COMP: 需要回调的实现 ITaskCallback 接口的类实例名称
     * + MSG_PARAM_TASK: 为对应之前的任务信息
     */

    ServiceCallbackHandler serviceCallbackHandler = new ServiceCallbackHandler(this);

    private static class ServiceCallbackHandler extends Handler {

        private final WeakReference<MainService> mainService;

        public ServiceCallbackHandler(MainService mainService) {
            this.mainService = new WeakReference<>(mainService);
        }

        @Override
        public void handleMessage(Message msg) {
            MainService mainService = this.mainService.get();
            if (mainService != null) {
                YHLog.v(mainService.tag(), "handleMessage - " + msg);

                Bundle bundle = msg.getData();
                if (null == bundle) {
                    // 至少需要绑定一个 component Name 参数
                    YHLog.w(mainService.tag(), "bundle is null!!!");
                    return;
                }

                String componentName = bundle.getString(MSG_PARAM_COMP);
                ITaskCallback component = mainService.getServiceCallbackComponent(componentName);
                if (null == component) {
                    YHLog.e(mainService.tag(), "not found component " + componentName + "|msg: " + msg.what);
                    return;
                }

                /**
                 * 存放需要递送至业务/UI层的数据
                 * KEY 值为各个业务接口定义的参数名称
                 */
                Map<String, Object> params = new HashMap<>();
                if (bundle.getSerializable(MSG_PARAM_TASK) != null) {
                    params.put(MSG_PARAM_TASK, bundle.getSerializable(MSG_PARAM_TASK));
                }

                if (msg.obj != null) {
                    params.put(MSG_PARAM_API_RESP, msg.obj);
                }

                component.refresh(msg.what, params);
            }
        }
    }

    /**
     * 为外部 Service 提供将任务信息通知给指定的 comp 的接口
     * <p/>
     * 如：KnowledgeService 在后台异步的从本地数据库中加载完内容列表后，
     * 需要通知给百科列表页面
     */
    public void notifyTaskFinish(LauncherTaskItem taskItem, ApiResponseResult responseResult) {

        Message msg = serviceCallbackHandler.obtainMessage();
        msg.what = taskItem.getTaskTypeId();
        msg.obj = responseResult;

        Bundle bundle = msg.getData();
        if (null == bundle) {
            bundle = new Bundle();
        }

        // 调用方组件完整路径名
        bundle.putString(MSG_PARAM_COMP, taskItem.getActivityName());
        bundle.putSerializable(MSG_PARAM_TASK, taskItem);
        msg.setData(bundle);

        serviceCallbackHandler.sendMessage(msg);
    }

    /**
     * 执行业务功能的异步任务
     * <p/>
     * 1, 一次可以提交多个按序执行的任务项(TaskItem)
     * 任务类型，参数等任务信息都封装在 TaskItem 中
     * 2, 为任务执行的进度
     * 3, 为返回最终执行完后的返回结果
     */
    public class ServiceTask extends AsyncTask<LauncherTaskItem, Integer, Object> {
        @Override
        protected Object doInBackground(LauncherTaskItem... params) {

            if (params == null || params.length == 0) {
                YHLog.v(tag(), "stop task, params invalid!!");
                return null;
            }

            LauncherTaskItem taskItem = params[0];
            String apiMethodName = taskTypeMethod.get(taskItem.getTaskTypeId());
            if (apiMethodName == null) {
                return null;
            }

            ApiResponseResult responseResult;

            // 通过反射机制获取相应任务类型所对应的 API 接口方法名
            try {
                Method apiMethod = HealthApi.class.getMethod(apiMethodName,
                        ApiBaseParam.class, Map.class);
                responseResult = (ApiResponseResult) apiMethod.invoke(
                        HealthApi.class, apiBaseParam, taskItem.getTaskParam());
            } catch (NoSuchMethodException e) {
                YHLog.e(tag(), "ServiceTask - NoSuchMethodException: " + e.getMessage());
                return null;
            } catch (InvocationTargetException e) {
                YHLog.e(tag(), "ServiceTask - InvocationTargetException: " + e.getMessage());
                return null;
            } catch (IllegalAccessException e) {
                YHLog.e(tag(), "ServiceTask - IllegalAccessException: " + e.getMessage());
                return null;
            }

            // 通知指定的 UI 组件
            notifyTaskFinish(taskItem, responseResult);

            return null;
        }
    }

    /**
     * 业务层/UI 层调用此接口启动一个异步任务来执行，该异步任务根据 task 信息来处理
     * 返回 true 只是表明 任务提交成功，并不表示任务会执行成功
     */
    public boolean startTask(LauncherTaskItem taskItem) {

        if (taskItem == null) {
            YHLog.w(tag(), "startTask - taskItem is null!");
            return false;
        }

        new ServiceTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, taskItem);
        YHLog.d(tag(), "startTask - " + taskItem);
        return true;
    }

    /**=============================
     * 下面放置和各业务相关的接口
     *==============================*/

    /*===== 资讯 ======*/

    /**
     * 获取最新资讯
     */
    public boolean getNews(String tag, int pagestart, int pagenum, String extraParam) {

        Map<String, Object> params = new HashMap<>();
        params.put(ApiConst.PARAM_PAGE_START, pagestart);
        params.put(ApiConst.PARAM_PAGE_NUM, pagenum);
        return startTask(new LauncherTaskItem(TaskType.GET_NEWS, params, extraParam,
                LauncherTaskPriority.NORMAL, tag));
    }

    /*===== 电子书 ======*/

    /**
     * 获取指定分类下的电子书列表
     */
    public boolean getEbooks(String tag, int catId, int mediatype, int pagestart, int pagenum, String extraParam) {
        Map<String, Object> params = new HashMap<>();
        params.put(ApiConst.PARAM_CAT_ID, catId);
        params.put(ApiConst.PARAM_MEDIA_TYPE, mediatype);
        params.put(ApiConst.PARAM_PAGE_START, pagestart);
        params.put(ApiConst.PARAM_PAGE_NUM, pagenum);
        return startTask(new LauncherTaskItem(TaskType.GET_EBOOKS, params, extraParam,
                LauncherTaskPriority.NORMAL, tag));
    }

    /*===== 语音频道 ======*/

    /**
     * 获取指定分类下的语音文件列表
     */
    public boolean getVoices(String tag, int catId, int mediatype, int pagestart, int pagenum, String extraParam) {
        Map<String, Object> params = new HashMap<>();
        params.put(ApiConst.PARAM_CAT_ID, catId);
        params.put(ApiConst.PARAM_MEDIA_TYPE, mediatype);
        params.put(ApiConst.PARAM_PAGE_START, pagestart);
        params.put(ApiConst.PARAM_PAGE_NUM, pagenum);
        return startTask(new LauncherTaskItem(TaskType.GET_VOICES, params, extraParam,
                LauncherTaskPriority.NORMAL, tag));
    }

}
