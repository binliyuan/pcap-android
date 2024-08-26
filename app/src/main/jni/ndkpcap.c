#include "com_example_zdl_NDKTools.h"
#include "include/pcap.h"
//#include "pcapd_priv.h"
#include <stdio.h>
#include <android/log.h>
#include <arpa/inet.h>
#define LOG   "libpcap"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG,__VA_ARGS__)

static char *dev;                        // 网络设备名称
static char errbuf[PCAP_ERRBUF_SIZE];    // 错误信息
static pcap_t *handle;                   // pcap 句柄
static pcap_dumper_t *dumpfile;          // pcap dump 文件指针

void packet_handler(u_char *dump_file, const struct pcap_pkthdr *header, const u_char *packet) {
    pcap_dump(dump_file, header, packet);  // 将数据包写入 pcap 文件
}


JNIEXPORT void JNICALL
Java_com_deepscience_example_NDKTools_pcapInit(JNIEnv *env, jclass clazz) {
    LOGD("pcapInit: ");
    dev = pcap_lookupdev(errbuf);
    if (dev == NULL) {
        LOGD(stderr, "无法找到默认设备: %s\n", errbuf);
        return;
    }
    LOGD("使用设备: %s\n", dev);
    // 2. 打开设备进行数据包捕获
    handle = pcap_open_live(dev, BUFSIZ, 1, 1000, errbuf);
    LOGD("设备 %s \n", errbuf);
    if (handle == NULL) {
        LOGD(stderr, "无法打开设备 %s: %s\n", dev, errbuf);
    }
}


JNIEXPORT void JNICALL
Java_com_deepscience_example_NDKTools_startCapture(JNIEnv *env, jclass clazz) {
    // 3. 打开 pcap 输出文件
    dumpfile = pcap_dump_open(handle, "output.pcap");
    if (dumpfile == NULL) {
        LOGD(stderr, "无法创建输出文件: %s\n", pcap_geterr(handle));
    }
    // 4. 开始捕获数据包
    LOGD("正在捕获数据包...\n");
    pcap_loop(handle, 10, packet_handler, (u_char *)dumpfile);
    // 5. 关闭 pcap dump 文件和 pcap 句柄
    LOGD("数据包捕获完成，已保存到 output.pcap 文件中。\n");
}

JNIEXPORT void JNICALL
Java_com_deepscience_example_NDKTools_stopCapture(JNIEnv *env, jclass clazz) {
    pcap_dump_close(dumpfile);
    pcap_close(handle);
}

JNIEXPORT void JNICALL
Java_com_deepscience_example_NDKTools_pcapPrint(JNIEnv *env, jclass clazz) {
    LOGD("pcapPrint: ");

    pcap_if_t *alldevs;
    pcap_if_t *d;
    char errbuf[PCAP_ERRBUF_SIZE];
    // 获取所有网络设备
    if (pcap_findalldevs(&alldevs, errbuf) == -1) {
        LOGD(stderr, "Error finding devices: %s\n", errbuf);
    }
    // 遍历并打印设备信息
    for (d = alldevs; d; d = d->next) {
        LOGD("Device: %s\n", d->name);
        if (d->description) {
            LOGD("Description: %s\n", d->description);
        } else {
            LOGD("Description: No description available\n");
        }

        // 打印接口的地址信息
        pcap_addr_t *a;
        for (a = d->addresses; a; a = a->next) {
            if (a) {
                char addr[INET_ADDRSTRLEN];
                char netmask[INET_ADDRSTRLEN];
                char dstaddr[INET_ADDRSTRLEN];
                char broadaddr[INET_ADDRSTRLEN];
                if ((a->addr) && inet_ntop(AF_INET, &((struct sockaddr_in *)a->addr)->sin_addr, addr, sizeof(addr))) {
                    LOGD("Address: %s\n", addr);
                }
                if ((a->netmask) && inet_ntop(AF_INET, &((struct sockaddr_in *)a->netmask)->sin_addr, netmask, sizeof(netmask))) {
                    LOGD("Netmask: %s\n", netmask);
                }
                if ((a->broadaddr) && inet_ntop(AF_INET, &((struct sockaddr_in *)a->broadaddr)->sin_addr, broadaddr, sizeof(broadaddr))) {
                    LOGD("broadaddr: %s\n", broadaddr);
                }
                if ((a->dstaddr) && inet_ntop(AF_INET, &((struct sockaddr_in *)a->dstaddr)->sin_addr, dstaddr, sizeof(dstaddr))) {
                    LOGD("dstaddr: %s\n", dstaddr);
                }
            }
        }
    }

    // 释放分配的设备链表内存
    pcap_freealldevs(alldevs);
}