package com.meta64.mobile.service;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meta64.mobile.config.JcrName;
import com.meta64.mobile.config.JcrProp;
import com.meta64.mobile.config.SessionContext;
import com.meta64.mobile.config.SpringContextUtil;
import com.meta64.mobile.lucene.FileIndexer;
import com.meta64.mobile.lucene.FileSearcher;
import com.meta64.mobile.model.FileSearchResult;
import com.meta64.mobile.model.RefInfo;
import com.meta64.mobile.request.FileSearchRequest;
import com.meta64.mobile.response.FileSearchResponse;
import com.meta64.mobile.user.UserManagerUtil;
import com.meta64.mobile.util.JcrUtil;
import com.meta64.mobile.util.ThreadLocals;

/* Performs searching against some Solr Client (over the network, like a microservice) */
@Component
public class LuceneService {
	private static final Logger log = LoggerFactory.getLogger(LuceneService.class);

	private static final ObjectMapper jsonMapper = new ObjectMapper();

	@Autowired
	private UserManagerService userManagerService;
	
	@Autowired
	private FileIndexer fileIndexer;
	
	@Autowired
	private FileSearcher searcher;

	public void reindex(Session session, FileSearchRequest req, FileSearchResponse res) throws Exception {
		if (session == null) {
			session = ThreadLocals.getJcrSession();
		}
		String nodeId = req.getNodeId();
		log.info("Reindex runing on server: "+nodeId);
		Node node = JcrUtil.findNode(session, nodeId);
		String path = JcrUtil.safeGetStringProp(node, "meta64:path");
		if (StringUtils.isEmpty(path)) {
			throw new Exception("No path specified to be indexed.");
		}
		fileIndexer.index(path, "txt");
	}
	
	public void search(Session session, FileSearchRequest req, FileSearchResponse res) throws Exception {
		if (session == null) {
			session = ThreadLocals.getJcrSession();
		}

		if (!userManagerService.isAllowFileSystemSearch()) {
			throw new Exception("File system search is not enabled on the server.");
		}

		List<FileSearchResult> resultList = searcher.search(req.getSearchText(), 100);
		
		String json = jsonMapper.writeValueAsString(resultList);
		log.debug("RESULT STRING: " + json);

		SessionContext sessionContext = (SessionContext) SpringContextUtil.getBean(SessionContext.class);
		String userName = sessionContext.getUserName();

		//RefInfo rootRefInfo = UserManagerUtil.getRootNodeRefInfoForUser(session, userName);

//		Node parentNode = JcrUtil.ensureNodeExists(session, rootRefInfo.getPath() + "/", JcrName.FILE_SEARCH_RESULTS, "Search Results");
//		parentNode.setProperty(JcrProp.CREATED_BY, userName);
//		Node newNode = parentNode.addNode(JcrUtil.getGUID(), JcrConstants.NT_UNSTRUCTURED);
//
//		// todo-0: need some type of way to make this kind of node not editable by user. It's
//		// presentation only, and not an editable node.
//		newNode.setProperty(JcrProp.CONTENT, "prop JSON_FILE_SEARCH_RESULT contains data.");
//		newNode.setProperty(JcrProp.CREATED_BY, userName);
//		newNode.setProperty(JcrProp.JSON_FILE_SEARCH_RESULT, json);
//		JcrUtil.timestampNewNode(session, newNode);
//		session.save();
//
//		res.setSearchResultNodeId(newNode.getIdentifier());
	}
}
