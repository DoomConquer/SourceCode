 /**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.zookeeper.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a class that implements prefix matching for 
 * components of a filesystem path. the trie
 * looks like a tree with edges mapping to 
 * the component of a path.
 * example /ab/bc/cf和/ab/cd对应的trie树
 *           /
 *        ab/
 *        (ab)
 *      bc/   \cd
 *       /     \
 *     (bc)   (cd)
 *   cf/
 *    /
 *  (cf)
 * trie树，主要用于保存配额结点路径
 */    
public class PathTrie {
    /**
     * the logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(PathTrie.class);
    
    /**
     * the root node of PathTrie
     */
    private final TrieNode rootNode; // 根结点

    // trie树结点
    static class TrieNode {
        boolean property = false; // 标识该结点是否有效（如果删除某个结点时，该结点还有子结点，那么标识该属性为false）
        final HashMap<String, TrieNode> children; // 子结点
        TrieNode parent = null; // 父结点

        /**
         * create a trienode with parent
         * as parameter
         * @param parent the parent of this trienode
         */
        private TrieNode(TrieNode parent) {
            children = new HashMap<String, TrieNode>();
            this.parent = parent;
        }
        
        /**
         * get the parent of this node
         * @return the parent node
         */
        TrieNode getParent() {
            return this.parent;
        }
        
        /**
         * set the parent of this node
         * @param parent the parent to set to
         */
        void setParent(TrieNode parent) {
            this.parent = parent;
        }
        
        /**
         * a property that is set 
         * for a node - making it 
         * special.
         */
        void setProperty(boolean prop) {
            this.property = prop;
        }
        
        /** the property of this
         * node 
         * @return the property for this
         * node
         */
        boolean getProperty() {
            return this.property;
        }

        /**
         * add a child to the existing node 添加子结点
         * @param childName the string name of the child
         * @param node the node that is the child
         */
        void addChild(String childName, TrieNode node) {
            synchronized(children) {
                if (children.containsKey(childName)) {
                    return;
                }
                children.put(childName, node);
            }
        }
     
        /**
         * delete child from this node 删除子结点
         * @param childName the string name of the child to 
         * be deleted
         */
        void deleteChild(String childName) {
            synchronized(children) {
                if (!children.containsKey(childName)) {
                    return;
                }
                TrieNode childNode = children.get(childName);
                // this is the only child node.
                if (childNode.getChildren().length == 1) { // 唯一的子结点
                    childNode.setParent(null);
                    children.remove(childName);
                } else {
                    // their are more child nodes
                    // so just reset property.
                    childNode.setProperty(false);
                }
            }
        }
        
        /**
         * return the child of a node mapping
         * to the input childname 获取子结点
         * @param childName the name of the child
         * @return the child of a node
         */
        TrieNode getChild(String childName) {
            synchronized(children) {
               if (!children.containsKey(childName)) {
                   return null;
               }
               else {
                   return children.get(childName);
               }
            }
        }

        /**
         * get the list of children of this 
         * trienode. 获取子结点列表
         * @return the string list of its children
         */
        String[] getChildren() {
           synchronized(children) {
               return children.keySet().toArray(new String[0]);
           }
        }
        
        /**
         * get the string representation
         * for this node 该结点子结点信息
         */
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Children of trienode: ");
            synchronized(children) {
                for (String str: children.keySet()) {
                    sb.append(" " + str);
                }
            }
            return sb.toString();
        }
    }
    
    /**
     * construct a new PathTrie with
     * a root node of /
     */
    public PathTrie() {
        this.rootNode = new TrieNode(null);
    }
    
    /**
     * add a path to the path trie 添加path路径到trie树，如果路径上的节点没有也会添加到trie树上
     * @param path
     */
    public void addPath(String path) {
        if (path == null) {
            return;
        }
        String[] pathComponents = path.split("/");
        TrieNode parent = rootNode;
        String part = null;
        if (pathComponents.length <= 1) {
            throw new IllegalArgumentException("Invalid path " + path);
        }
        for (int i = 1; i < pathComponents.length; i++) {
            part = pathComponents[i];
            if (parent.getChild(part) == null) { // 子结点没在parent的children列表中，添加到列表中
                parent.addChild(part, new TrieNode(parent));
            }
            parent = parent.getChild(part); // 父结点指向该结点，继续添加路径上的后续子结点
        }
        parent.setProperty(true);
    }
    
    /**
     * delete a path from the trie 从trie树中删除path路径，只删除路径最后子结点，不会路径上的父结点
     * @param path the path to be deleted
     */
    public void deletePath(String path) {
        if (path == null) {
            return;
        }
        String[] pathComponents = path.split("/");
        TrieNode parent = rootNode;
        String part = null;
        if (pathComponents.length <= 1) { 
            throw new IllegalArgumentException("Invalid path " + path);
        }
        for (int i = 1; i < pathComponents.length; i++) {
            part = pathComponents[i];
            if (parent.getChild(part) == null) { // 当前结点在trie树上不存在，直接返回
                //the path does not exist 
                return;
            }
            parent = parent.getChild(part);
            LOG.info("{}",parent);
        }
        TrieNode realParent  = parent.getParent(); // 直接父结点
        realParent.deleteChild(part); // 删除该结点
    }
    
    /**
     * return the largest prefix for the input path.
     * 查找path路径在trie树上的最长前缀路径，必须保证path路径上结点在trie树上是有效结点。如果存在配额结点/zookeeper/quota/a/b，
     * path等于/zookeeper/quota/a/b/c/zookeeper_stats，那么最大前缀为/zookeeper/quota/a/b。但是如果存在配额结点
     * /zookeeper/quota/a/b/d，path等于/zookeeper/quota/a/b/c/zookeeper_stats的最大公共前缀为空，因为配额结点只有叶结点d
     * 是有效的，中间的节点property都等于false
     *
     * @param path the input path
     * @return the largest prefix for the input path.
     */
    public String findMaxPrefix(String path) {
        if (path == null) {
            return null;
        }
        if ("/".equals(path)) { // 根结点
            return path;
        }
        String[] pathComponents = path.split("/");
        TrieNode parent = rootNode;
        List<String> components = new ArrayList<String>();
        if (pathComponents.length <= 1) {
            throw new IllegalArgumentException("Invalid path " + path);
        }
        int i = 1;
        String part = null;
        StringBuilder sb = new StringBuilder();
        int lastindex = -1; // 记录上一次有效结点位置
        while((i < pathComponents.length)) {
            if (parent.getChild(pathComponents[i]) != null) {
                part = pathComponents[i];
                parent = parent.getChild(part);
                components.add(part);
                if (parent.getProperty()) { // property标识该结点是否有效
                    lastindex = i - 1;
                }
            } else {
                break;
            }
            i++;
        }
        for (int j = 0; j < (lastindex + 1); j++) {
            sb.append("/" + components.get(j));
        }
        return sb.toString();
    }

    /**
     * clear all nodes 清空trie树中所有结点
     */
    public void clear() {
        for(String child : rootNode.getChildren()) {
            rootNode.deleteChild(child);
        }
    }
}
