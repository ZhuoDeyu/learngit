#include<stdio.h>
#include<android/log.h>
#include<jni.h>
#include<stdlib.h>
#include <memory.h>
#include <unistd.h>
#include<time.h>
#include<assert.h>
#define LOG_TAG "systemmonitorndk"
#define CK_TIME 1
#define BUF_SIZE 1024
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

JNIEXPORT jintArray  JNICALL
Java_com_starnet_systemmonitor_ServiceReader_getMemFromC(
    JNIEnv* env,  jobject thiz, jintArray arr)
{
    jsize len = (*env)->GetArrayLength(env, arr);
    	//在java中申请一块内存  以用来将C的数组传输给java程序
    jintArray ret=(*env)->NewIntArray(env,len);
        //获取传入的数组
    jint *pArr = (*env)->GetIntArrayElements(env, arr, 0);
    FILE *stream = NULL;
    char str[256] = {'\0'};
    char msg[256] = {'\0'};
    char *p_str = NULL;
    char *p_msg = NULL;

    stream = fopen("/proc/meminfo", "rb");
    if(stream != NULL)
    {
        fgets(msg,256,stream);
        p_msg = msg;
        p_str = str;
        while(*p_msg != '\0')
        {
            if('0' <= *p_msg && *p_msg <= '9')
            {
                *p_str = *p_msg;
                p_str++;
            }
                p_msg++;
        }
        *pArr = atoi(str);
        memset(str,0,256);

        fgets(msg,256,stream);
        p_str = str;
        p_msg = msg;
        while(*p_msg != '\0')
        {
            if('0' <= *p_msg && *p_msg <= '9')
            {
                *p_str = *p_msg;
                p_str++;
            }
            p_msg++;
        }
        *(pArr + 1) = atoi(str);
        memset(str,0,256);

        fgets(msg,256,stream);
        p_str = str;
        p_msg = msg;
        while(*p_msg != '\0')
        {
            if('0' <= *p_msg && *p_msg <= '9')
            {
                *p_str = *p_msg;
                p_str++;
            }
                p_msg++;
        }
        *(pArr + 2) = atoi(str);
        memset(str,0,256);

        fgets(msg,256,stream);
        p_str = str;
        p_msg = msg;
        while(*p_msg != '\0')
        {
             if('0' <= *p_msg && *p_msg <= '9')
             {
                  *p_str = *p_msg;
                  p_str++;
             }
             p_msg++;
        }
        *(pArr + 3) = atoi(str);
        memset(str,0,256);

        fgets(msg,256,stream);
        fgets(msg,256,stream);
        p_str = str;
        p_msg = msg;
        while(*p_msg != '\0')
        {
             if('0' <= *p_msg && *p_msg <= '9')
             {
                  *p_str = *p_msg;
                  p_str++;
             }
             p_msg++;
        }
         *(pArr + 4) = atoi(str);
         memset(str,0,256);

        fgets(msg,256,stream);
        p_str = str;
        p_msg = msg;
        while(*p_msg != '\0')
        {
             if('0' <= *p_msg && *p_msg <= '9')
             {
                  *p_str = *p_msg;
                  p_str++;
             }
             p_msg++;
             }
         *(pArr + 5) = atoi(str);

        fclose(stream);
    }else{
        LOGD("get MemTotal failed!");
        sprintf(str, "0000000");
    }
    //将C的数组拷贝给java中的数组
    (*env)->SetIntArrayRegion(env,ret,0,len,pArr);
    return ret;
}


JNIEXPORT jlongArray JNICALL
Java_com_starnet_systemmonitor_ServiceReader_getCpuUsageFromC(
    JNIEnv* env,  jobject thiz, jlongArray cpuLong)
{
    FILE *fp;
    char buf[128];
    char cpu[5];
    long user,nice,sys,idle,iowait,irq,softirq;
    long pid,utime,stime,cutime,cstime;
    long all1,all2,idle1,idle2,ptotal1,ptotal2;

    fp = fopen("/proc/stat","r");
    if(fp == NULL)
    {
        perror("fopen:");
        exit (0);
    }
        fgets(buf,sizeof(buf),fp);
        sscanf(buf,"%s%ld%ld%ld%ld%ld%ld%ld",cpu,&user,&nice,&sys,&idle,&iowait,&irq,&softirq);
        all1 = user+nice+sys+idle+iowait+irq+softirq;
        idle1 = idle;
        fclose(fp);

        char file[64] = {0};//文件名
        char line_buff[1024] = {0};  //读取行的缓冲区
        sprintf(file,"/proc/%d/stat",getpid());//文件中第11行包含着
        fprintf (stderr, "current pid:%d\n", getpid());
        fp = fopen (file, "r");
        //以R读的方式打开文件再赋给指针fd
        fgets (line_buff, sizeof(line_buff), fp);
        //从fd文件中读取长度为buff的字符串再存到起始地址为buff这个空间里

        sscanf(line_buff,"%ld",&pid);//取得第一项

        //取得从第14项开始的起始指针
        assert(line_buff);
        char* q = line_buff;
        int plen = strlen(line_buff);
        int count = 0;//统计空格数

        for (int i=0; i<plen; i++)
        {
            if (' ' == *q)
            {
                count++;
                if (count == 13)
                {
                    q++;
                    break;
                }
            }
            q++;
        }
        sscanf(q,"%ld %ld %ld %ld ",&utime,&stime,&cutime,&cstime);//格式化第14,15,16,17项

        fprintf (stderr, "====pid%ld:%ld%ld%ld%ld====\n", pid, utime,stime,cutime,cstime);
        fclose(fp);     //关闭文件fd
        LOGD("pid%ld:%ld %ld %ld %ld",pid,utime,stime,cutime,cstime);
        ptotal1 = utime + stime + cutime +  cstime;

        fp = fopen("/proc/stat","r");
        /*第二次取数据*/
        sleep(1);
        memset(buf,0,sizeof(buf));
        cpu[0] = '\0';
        user=nice=sys=idle=iowait=irq=softirq=0;
        fgets(buf,sizeof(buf),fp);
        sscanf(buf,"%s%ld%ld%ld%ld%ld%ld%ld",cpu,&user,&nice,&sys,&idle,&iowait,&irq,&softirq);
        all2 = user+nice+sys+idle+iowait+irq+softirq;
        idle2 = idle;
        fclose(fp);

        char line_buff2[1024] = {0};  //读取行的缓冲区
        sprintf(file,"/proc/%d/stat",getpid());//文件中第11行包含着
        fprintf (stderr, "current pid:%d\n", getpid());
        fp = fopen (file, "r");
        //以R读的方式打开文件再赋给指针fd
        fgets (line_buff2, sizeof(line_buff2), fp);
        //从fd文件中读取长度为buff的字符串再存到起始地址为buff这个空间里
        sscanf(line_buff2,"%ld",&pid);//取得第一项

        //取得从第14项开始的起始指针
        assert(line_buff2);
        q = line_buff2;
        plen = strlen(line_buff2);
        count = 0;//统计空格数
        for (int i=0; i<plen; i++)
        {
            if (' ' == *q)
            {
                count++;
                if (count == 13)
                {
                     q++;
                     break;
                }
            }
            q++;
        }
        sscanf(q,"%ld %ld %ld %ld ",&utime,&stime,&cutime,&cstime);//格式化第14,15,16,17项

        fprintf (stderr, "====pid%ld:%ld%ld%ld%ld====\n", pid, utime,stime,cutime,cstime);
        fclose(fp);     //关闭文件fd
        LOGD("--2---pid%ld:%ld %ld %ld %ld",pid,utime,stime,cutime,cstime);
        ptotal2 = utime + stime + cutime +  cstime;

        jsize len = (*env)->GetArrayLength(env, cpuLong);
            	//在java中申请一块内存  以用来将C的数组传输给java程序
        jlongArray ret=(*env)->NewLongArray(env,len);
                //获取传入的数组
        jlong *pArr = (*env)->GetLongArrayElements(env, cpuLong, 0);

        *(pArr+0) = all1;
        *(pArr+1) = idle1;
        *(pArr+2) = all2;
        *(pArr+3) = idle2;
        *(pArr+4) = ptotal1;
        *(pArr+5) = ptotal2;
        (*env)->SetLongArrayRegion(env,ret,0,len,pArr);

     return ret;
}

JNIEXPORT jintArray  JNICALL
Java_com_starnet_systemmonitor_ServiceReader_getMemJavaCallback(
    JNIEnv* env,  jobject thiz)
{
    jclass clazz = (*env)->FindClass(env,"com/starnet/systemmonitor/ServiceReader");
    if(clazz == 0)
    {
        LOGD("find class error");
        return 0;
    }
    jmethodID getMem = (*env)->GetMethodID(env,clazz,"getMemFromJava","()[I");
    if(getMem == 0)
    {
        LOGD("FIND GETMEM ERROR");
        return 0;
    }
    jobject object = (*env)->CallObjectMethod(env,thiz,getMem);
    jintArray result = (jintArray)object;
    return result;
}


//计算逻辑，求expression表达式的值
JNIEXPORT jstring  JNICALL
Java_com_starnet_systemmonitor_ServiceReader_getResult(
    JNIEnv* env,  jobject thiz,jstring contentStr)
{
       char* expression = NULL;

           jclass     jstrObj   = (*env)->FindClass(env, "java/lang/String");
           jstring    encode    = (*env)->NewStringUTF(env, "utf-8");
           jmethodID  methodId  = (*env)->GetMethodID(env, jstrObj, "getBytes", "(Ljava/lang/String;)[B");
           jbyteArray byteArray = (jbyteArray)(*env)->CallObjectMethod(env, contentStr, methodId, encode);
           jsize      strLen    = (*env)->GetArrayLength(env, byteArray);
           jbyte      *jBuf     = (*env)->GetByteArrayElements(env, byteArray, JNI_FALSE);

           if (jBuf > 0)
           {
               expression = (char*)malloc(strLen + 1);

               if (!expression)
               {
                   return NULL;
               }

               memcpy(expression, jBuf, strLen);

               expression[strLen] = 0;
           }

           (*env)->ReleaseByteArrayElements(env, byteArray, jBuf, 0);

        double result=0;
        double tNum=1,lowNum=0.1,num=0;
        char tmp;
        int operate = 1; //识别+-*/，为+时为正数，为-时为负数，为×时为-2/2,为/时为3/-3;
        jboolean point = JNI_FALSE;
        jboolean divisorIsZero = JNI_FALSE;
        int len = strlen(expression);
        for(int i=0;i<len;i++){
            //遍历表达式
            tmp = expression[i];
            if(tmp=='.'){
                //因为可能出现小数，此处进行判断是否有小数出现
                point = JNI_TRUE;
                lowNum = 0.1;
            }else if(tmp=='+'||tmp=='-'){
                if(operate!=3&&operate!=-3){
                    //此处判断通用，适用于+-*
                    tNum *= num;
                }else{ //计算/
                    if(num == 0)
                    {
                        return (*env)->NewStringUTF(env,"除数不能为零");
                    }
                    tNum /= num;
                }
                if(operate<0){
                    //累加入最终的结果
                    result -= tNum;
                }else{
                    result += tNum;
                }
                operate = tmp=='+'?1:-1;
                num = 0;
                tNum = 1;
                point = JNI_FALSE;
            }else if(tmp=='*'){
                if(operate!=3&&operate!=-3){
                    tNum *= num;
                }else{
                    tNum /= num;
                }
                operate = operate<0?-2:2;
                point = JNI_FALSE;
                num = 0;
            }else if(tmp=='/'){
                if(operate!=3&&operate!=-3){
                    tNum *= num;
                }else{
                    if(num == 0){
                        divisorIsZero = JNI_TRUE;
                    }else{
                        tNum /= num;
                    }
                }
                operate = operate<0?-3:3;
                point = JNI_FALSE;
                num = 0;
            }else{
                //读取expression中的每个数字，doube型
                if(!point){
                    num = num*10+tmp-'0';
                }else{
                    num += (tmp-'0')*lowNum;
                    lowNum*=0.1;
                }
            }
        }
        //循环遍历结束，计算最后一个运算符后面的数
        if(operate!=3&&operate!=-3){
            tNum *= num;
        }else{
            tNum /= num;
            if(num == 0){
                divisorIsZero = JNI_TRUE;
            }
        }
        if(operate<0){
            result -= tNum;
        }else{
            result += tNum;
        }
        //返回最后的结果
        if(!divisorIsZero){
            char *stringResult = (char *) malloc(sizeof(char) * 16);
            sprintf(stringResult,"%f",result);
            LOGD("--------------%s",stringResult);
            return (*env)->NewStringUTF(env,stringResult);
        }else{
            return (*env)->NewStringUTF(env,"除数不能为零");
            LOGD("divisor is zero");
        }
}

JNIEXPORT jint  JNICALL
Java_com_starnet_systemmonitor_ServiceReader_getPid(
    JNIEnv* env,  jobject thiz)
{
    pid_t pid = getpid();
    return pid;
}

JNIEXPORT jstring  JNICALL
Java_com_starnet_systemmonitor_ServiceReader_getPidName(
    JNIEnv* env,  jobject thiz)
{
    pid_t pid = getpid();
    char proc_pid_path[BUF_SIZE];
    char buf[BUF_SIZE];
    sprintf(proc_pid_path, "/proc/%d/status", pid);
    FILE* fp = fopen(proc_pid_path, "r");
    if(NULL != fp){
        if( fgets(buf, BUF_SIZE-1, fp)== NULL ){
            return (*env)->NewStringUTF(env,"失败");
        }
        fclose(fp);
        LOGD("0------0%s",buf);
    }
    return (*env)->NewStringUTF(env,buf);
}

