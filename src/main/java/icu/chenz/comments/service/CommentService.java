package icu.chenz.comments.service;

import icu.chenz.comments.dao.CommentDao;
import icu.chenz.comments.dao.UserDao;
import icu.chenz.comments.entity.CommentEntity;
import icu.chenz.comments.entity.Entity;
import icu.chenz.comments.utils.exception.BadRequest;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author : Chenz
 * @date : 2023-01-09 18:19
 */

@Service
public class CommentService {
    @Resource
    private CommentDao commentDao;

    @Resource
    private UserDao userDao;

    public Map<String, List<? extends Entity>> getByContext(String context) {
        HashMap<String, List<? extends Entity>> res = new HashMap<>(2);
        List<CommentEntity> comments = commentDao.getByContext(context);
        res.put("comments", comments);
        HashSet<Long> query = new HashSet<>(comments.stream().map(CommentEntity::getUser).toList());
        comments.stream()
                .map(CommentEntity::getSubComments)
                .forEach(item -> query.addAll(item.stream().map(CommentEntity::getUser).toList()));
        res.put("users", userDao.getByIds(query));
        return res;
    }

    public int delete(Long user, Long id) throws BadRequest {
        CommentEntity comment = commentDao.getById(id);
        if (!comment.getUser().equals(user)) {
            throw new BadRequest("不能删除别人的评论哦");
        }
        return commentDao.delete(id);
    }

    public CommentEntity comment(CommentEntity entity) throws BadRequest {
        if (commentDao.getContextString(entity.getContext()) == null) {
            throw new BadRequest("未知文章");
        }
        commentDao.create(entity);
        return entity;
    }
}