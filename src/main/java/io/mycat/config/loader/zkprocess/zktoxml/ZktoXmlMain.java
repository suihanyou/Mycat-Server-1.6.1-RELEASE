package io.mycat.config.loader.zkprocess.zktoxml;

import io.mycat.config.loader.console.ZookeeperPath;
import io.mycat.config.loader.zkprocess.comm.ZkConfig;
import io.mycat.config.loader.zkprocess.comm.ZkParamCfg;
import io.mycat.config.loader.zkprocess.comm.ZookeeperProcessListen;
import io.mycat.config.loader.zkprocess.console.ZkNofiflyCfg;
import io.mycat.config.loader.zkprocess.parse.XmlProcessBase;
import io.mycat.config.loader.zkprocess.zktoxml.listen.BinDataPathChildrenCacheListener;
import io.mycat.config.loader.zkprocess.zktoxml.listen.EcacheszkToxmlLoader;
import io.mycat.config.loader.zkprocess.zktoxml.listen.RuleDataPathChildrenCacheListener;
import io.mycat.config.loader.zkprocess.zktoxml.listen.RuleszkToxmlLoader;
import io.mycat.config.loader.zkprocess.zktoxml.listen.SchemaszkToxmlLoader;
import io.mycat.config.loader.zkprocess.zktoxml.listen.SequenceTopropertiesLoader;
import io.mycat.config.loader.zkprocess.zktoxml.listen.ServerzkToxmlLoader;
import io.mycat.config.loader.zkprocess.zookeeper.process.ZkMultLoader;
import io.mycat.config.model.UserConfig;
import io.mycat.config.util.FastJSONUtils;
import io.mycat.util.ZKUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooDefs.Perms;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 将xk的信息转换为xml文件的操作 源文件名：ZktoxmlMain.java 文件版本：1.0.0 创建作者：liujun
 * 创建日期：2016年9月20日 修改作者：liujun 修改日期：2016年9月20日 文件描述：TODO 版权所有：Copyright 2016
 * zjhz, Inc. All Rights Reserved.
 */
public class ZktoXmlMain {

	/**
	 * 日志
	 * 
	 * @字段说明 LOGGER
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ZkMultLoader.class);

	public static void main(String[] args) throws Exception {
		// loadZktoFile();
		System.out.println(Long.MAX_VALUE);
	}

	// 获得zk的连接信息
	private static CuratorFramework zkConn = null;
	private static String basePath = null;

	/**
	 * 将zk数据放到到本地 方法描述
	 * 
	 * @throws Exception
	 * @创建日期 2016年9月21日
	 */
	public static void loadZktoFile() throws Exception {

		// 加载zk总服务
		ZookeeperProcessListen zkListen = new ZookeeperProcessListen();

		// 得到集群名称
		String custerName = ZkConfig.getInstance().getValue(
				ZkParamCfg.ZK_CFG_CLUSTERID);
		// 得到基本路径
		basePath = ZookeeperPath.ZK_SEPARATOR.getKey() + custerName;
		zkListen.setBasePath(basePath);
		String id0 = ZkConfig.getInstance().getValue(ZkParamCfg.ACL_ID0)
				.equals("") ? "wang" : ZkConfig.getInstance().getValue(
				ZkParamCfg.ACL_ID0);
		String id1 = ZkConfig.getInstance().getValue(ZkParamCfg.ACL_ID1)
				.equals("") ? "yang" : ZkConfig.getInstance().getValue(
				ZkParamCfg.ACL_ID1);
		zkConn = buildConnection(
				ZkConfig.getInstance().getValue(ZkParamCfg.ZK_CFG_URL), id0,
				id1);
		if (Boolean.parseBoolean(ZkConfig.getInstance().getValue(
				ZkParamCfg.ZK_CFG_FLAG))) {
			// 获得公共的xml转换器对象
			XmlProcessBase xmlProcess = new XmlProcessBase();

			// 加载以接收者
			new SchemaszkToxmlLoader(zkListen, zkConn, xmlProcess);

			// server加载
			new ServerzkToxmlLoader(zkListen, zkConn, xmlProcess);

			// rule文件加载
			new RuleszkToxmlLoader(zkListen, zkConn, xmlProcess);

			// 将序列配制信息加载
			new SequenceTopropertiesLoader(zkListen, zkConn, xmlProcess);

			// 进行ehcache转换
			new EcacheszkToxmlLoader(zkListen, zkConn, xmlProcess);

			// 将bindata目录的数据进行转换到本地文件
			ZKUtils.addChildPathCache(ZKUtils.getZKBasePath() + "bindata",
					new BinDataPathChildrenCacheListener());

			// ruledata
			ZKUtils.addChildPathCache(ZKUtils.getZKBasePath() + "ruledata",
					new RuleDataPathChildrenCacheListener());

			// 初始化xml转换操作
			xmlProcess.initJaxbClass();

			// 通知所有人
			zkListen.notifly(ZkNofiflyCfg.ZK_NOTIFLY_LOAD_ALL.getKey());

			// 加载watch
			loadZkWatch(zkListen.getWatchPath(), zkConn, zkListen);

			// 创建临时节点
			createTempNode("/mycat/mycat-cluster-1/line", "tmpNode1", zkConn);
		}
	}

	/**
	 * 把mycat 节点的ip注册到zookeeper 上面
	 * 
	 * @author shy
	 * */
	public static void registerMycat(int port,
			Map<String, UserConfig> userConfigs) throws Exception {

		// 父节点 集群id/注册节点名称/
		String parPath = basePath
				+ ZkConfig.getInstance().getValue(ZkParamCfg.ZK_REG_NAME);
		String nodeInfo = ZkConfig.getInstance().getValue(
				ZkParamCfg.NODE_IP_STRING)
				+ ":" + port;
		String subPath = ZKPaths.makePath(parPath, nodeInfo);
		String clientPath = ZKPaths.makePath(basePath
				+ ZkConfig.getInstance().getValue(ZkParamCfg.ZK_CLIENT_NODE),
				nodeInfo);
		if (zkConn.checkExists().forPath(parPath) == null) {
			// 如果父节点不存在，创建父节点
			zkConn.create().creatingParentsIfNeeded()
					.withMode(CreateMode.PERSISTENT).forPath(parPath);
		}
		if (zkConn.checkExists().forPath(clientPath) == null) {
			// 如果客户端注册节点不存在，创建
			zkConn.create().creatingParentsIfNeeded()
					.withMode(CreateMode.PERSISTENT).forPath(clientPath);
		}
		List<String> clients = zkConn.getChildren().forPath(clientPath);
		if (clients != null && clients.size() != 0) {
			// 存在子节点，说明有其他注册了相同的mycat节点，并且在使用中
			throw new RuntimeException("注册失败，已经存在相同的注册节点");
		}
		// 如果临时节点存在，并且需要立即注册
		if (zkConn.checkExists().forPath(subPath) != null) {
			try {
				// 如果子节点存在，尝试删除子节点，一般是两次连续启动，临时节点还没有从zk上删除
				zkConn.delete().forPath(subPath);
			} catch (Exception e) {
				// do nothing 可能是临时节点自己消失了
			}
		}

		// 创建临时子节点，注册mycat,ip+port 确定唯一一个节点，防止一个机器上启动多个mycat
		zkConn.create()
				.withMode(CreateMode.EPHEMERAL)
				.forPath(
						subPath,
						FastJSONUtils.toJSONByteArray(new MycatNodeVO(nodeInfo,
								ZkConfig.getInstance().getValue(
										ZkParamCfg.NODE_WEIGHT), userConfigs)));
	}

	private static void loadZkWatch(Set<String> setPaths,
			final CuratorFramework zkConn, final ZookeeperProcessListen zkListen)
			throws Exception {

		if (null != setPaths && !setPaths.isEmpty()) {
			for (String path : setPaths) {
				runWatch(zkConn, path, zkListen);

				LOGGER.info("ZktoxmlMain loadZkWatch path:" + path
						+ " regist success");
			}
		}
	}

	/**
	 * 创建临时节点测试 方法描述
	 * 
	 * @param parent
	 * @param node
	 * @param zkConn
	 * @throws Exception
	 * @创建日期 2016年9月20日
	 */
	private static void createTempNode(String parent, String node,
			final CuratorFramework zkConn) throws Exception {

		String path = ZKPaths.makePath(parent, node);

		zkConn.create().withMode(CreateMode.EPHEMERAL).inBackground()
				.forPath(path);

	}

	/**
	 * 进行zk的watch操作 方法描述
	 * 
	 * @param zkConn
	 *            zk的连接信息
	 * @param path
	 *            路径信息
	 * @param zkListen
	 *            监控路径信息
	 * @throws Exception
	 * @创建日期 2016年9月20日
	 */
	private static void runWatch(final CuratorFramework zkConn, String path,
			final ZookeeperProcessListen zkListen) throws Exception {
		zkConn.getData().usingWatcher(new Watcher() {

			@Override
			public void process(WatchedEvent event) {
				LOGGER.info("ZktoxmlMain runWatch  process path receive event:"
						+ event);

				String path = ZookeeperPath.ZK_SEPARATOR.getKey()
						+ event.getPath();
				// 进行通知更新
				zkListen.notifly(path);

				LOGGER.info("ZktoxmlMain runWatch  process path receive event:"
						+ event + " notifly success");

				try {
					// 重新注册监听
					runWatch(zkConn, path, zkListen);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}).inBackground().forPath(path);
	}

	private static CuratorFramework buildConnection(String url,
			final String id0, final String id1) {
		ACLProvider aclProvider = new ACLProvider() {
			private List<ACL> acl;

			@Override
			public List<ACL> getDefaultAcl() {
				if (acl == null) {
					ArrayList<ACL> acl = ZooDefs.Ids.CREATOR_ALL_ACL;
					acl.clear();
					acl.add(new ACL(Perms.ALL, new Id("auth", id0 + ":" + id1)));
					this.acl = acl;
				}
				return acl;
			}

			@Override
			public List<ACL> getAclForPath(String path) {
				return acl;
			}
		};
		CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
				.aclProvider(aclProvider).connectString(url)
				.authorization("digest", (id0 + ":" + id1).getBytes())
				.retryPolicy(new ExponentialBackoffRetry(100, 3)).build();

		// start connection
		curatorFramework.start();
		// wait 3 second to establish connect
		try {
			curatorFramework.blockUntilConnected(3, TimeUnit.SECONDS);
			if (curatorFramework.getZookeeperClient().isConnected()) {
				return curatorFramework
						.usingNamespace(ZookeeperPath.FLOW_ZK_PATH_BASE
								.getKey());
			}
		} catch (InterruptedException ignored) {
			Thread.currentThread().interrupt();
		}

		// fail situation
		curatorFramework.close();
		throw new RuntimeException("failed to connect to zookeeper service : "
				+ url);
	}
}
