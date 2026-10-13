import { useState } from 'react';
import { Heart, MessageCircle, Share2, Check, Send, Trash2, User } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import apiClient from '../../api/apiClient';
import { useAuth } from '../../context/AuthContext';
import { getApiErrorMessage } from '../../utils/apiError';
import RatingStars from '../common/RatingStars';
import FoodImage from '../recipe/FoodImage';
import Spinner from '../common/Spinner';

export default function CommunityPostCard({
  id,
  username = 'Chef',
  displayName,
  avatar,
  image,
  recipeId,
  recipeTitle,
  likes = 0,
  comments = 0,
  rating = 5.0,
  caption = '',
  isLikedByCurrentUser = false,
  currentUserRating = null,
  className = ''
}) {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();

  const [likeCount, setLikeCount] = useState(likes);
  const [isLiked, setIsLiked] = useState(isLikedByCurrentUser);
  const [isLiking, setIsLiking] = useState(false);

  const [currentRating, setCurrentRating] = useState(rating);
  const [userRating, setUserRating] = useState(currentUserRating);
  const [isRating, setIsRating] = useState(false);

  const [commentCount, setCommentCount] = useState(comments);
  const [showComments, setShowComments] = useState(false);
  const [commentsList, setCommentsList] = useState([]);
  const [loadingComments, setLoadingComments] = useState(false);
  const [newCommentText, setNewCommentText] = useState('');
  const [isSubmittingComment, setIsSubmittingComment] = useState(false);

  const [copied, setCopied] = useState(false);

  // Toggle Like
  const toggleLike = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    if (isLiking) return;

    setIsLiking(true);
    const previousLiked = isLiked;
    const previousCount = likeCount;

    // Optimistic update
    setIsLiked(!previousLiked);
    setLikeCount(previousLiked ? previousCount - 1 : previousCount + 1);

    try {
      if (previousLiked) {
        const res = await apiClient.delete(`/community/posts/${id}/like`);
        setLikeCount(res.data.likeCount);
        setIsLiked(res.data.liked);
      } else {
        const res = await apiClient.post(`/community/posts/${id}/like`);
        setLikeCount(res.data.likeCount);
        setIsLiked(res.data.liked);
      }
    } catch {
      // Revert on failure
      setIsLiked(previousLiked);
      setLikeCount(previousCount);
    } finally {
      setIsLiking(false);
    }
  };

  // Toggle and load comments
  const handleToggleComments = async () => {
    const nextState = !showComments;
    setShowComments(nextState);

    if (nextState && commentsList.length === 0) {
      setLoadingComments(true);
      try {
        const res = await apiClient.get(`/community/posts/${id}/comments`);
        setCommentsList(res.data || []);
      } catch (err) {
        console.error('Failed to load comments:', err);
      } finally {
        setLoadingComments(false);
      }
    }
  };

  // Submit a new comment
  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    if (!newCommentText.trim() || isSubmittingComment) return;

    setIsSubmittingComment(true);
    try {
      const res = await apiClient.post(`/community/posts/${id}/comments`, {
        content: newCommentText.trim()
      });
      setCommentsList((prev) => [...prev, res.data]);
      setCommentCount((prev) => prev + 1);
      setNewCommentText('');
    } catch (err) {
      console.error('Failed to post comment:', err);
    } finally {
      setIsSubmittingComment(false);
    }
  };

  // Delete a comment
  const handleDeleteComment = async (commentId) => {
    try {
      await apiClient.delete(`/community/comments/${commentId}`);
      setCommentsList((prev) => prev.filter((c) => c.id !== commentId));
      setCommentCount((prev) => Math.max(0, prev - 1));
    } catch (err) {
      console.error('Failed to delete comment:', err);
    }
  };

  // Rate this post
  const handleRate = async (score) => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    if (isRating) return;

    setIsRating(true);
    try {
      const res = await apiClient.post(`/community/posts/${id}/rating`, { score });
      setCurrentRating(res.data.averageRating);
      setUserRating(res.data.currentUserRating);
    } catch (err) {
      console.error('Failed to submit rating:', err);
    } finally {
      setIsRating(false);
    }
  };

  const handleShare = () => {
    navigator.clipboard?.writeText(window.location.origin + `/community`);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const authorInitial = (displayName || username || 'C').charAt(0).toUpperCase();

  return (
    <article className={`bg-white border border-[#D5D8D5] rounded-2xl overflow-hidden shadow-xs flex flex-col ${className}`}>
      {/* Post Header: Avatar & Username */}
      <div className="p-4 flex items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          {avatar ? (
            <img
              src={avatar}
              alt={username}
              className="w-10 h-10 rounded-full object-cover border border-[#D5D8D5]"
            />
          ) : (
            <div className="w-10 h-10 rounded-full bg-[#E8F4E9] text-[#3F8F49] font-bold text-sm flex items-center justify-center border border-[#5AAE62]/30">
              {authorInitial}
            </div>
          )}
          <div>
            <span className="font-bold text-sm text-[#111111] block">
              {displayName || `@${username}`}
            </span>
            {recipeTitle && (
              <Link
                to={recipeId ? `/recipe/${recipeId}` : '#'}
                className="text-xs text-[#5AAE62] font-semibold hover:underline line-clamp-1"
              >
                Remake of: {recipeTitle}
              </Link>
            )}
          </div>
        </div>
      </div>

      {/* Main Food Image */}
      <div className="relative aspect-4/3 overflow-hidden bg-[#F5F6F5]">
        <FoodImage
          src={image}
          alt={caption || 'Community Remake'}
          className="rounded-none border-none aspect-4/3"
        />
      </div>

      {/* Action Row */}
      <div className="p-4 flex-1 flex flex-col justify-between">
        <div>
          <div className="flex items-center justify-between text-[#555555] mb-3">
            <div className="flex items-center gap-4">
              <button
                type="button"
                onClick={toggleLike}
                disabled={isLiking}
                className={`flex items-center gap-1.5 text-sm font-medium transition-colors ${
                  isLiked ? 'text-red-500 font-bold' : 'hover:text-[#111111]'
                }`}
                aria-label="Like post"
              >
                <Heart className={`w-5 h-5 ${isLiked ? 'fill-red-500 text-red-500' : ''}`} />
                <span>{likeCount}</span>
              </button>

              <button
                type="button"
                onClick={handleToggleComments}
                className={`flex items-center gap-1.5 text-sm font-medium transition-colors ${
                  showComments ? 'text-[#3F8F49] font-bold' : 'hover:text-[#111111]'
                }`}
                aria-label="Comment on post"
              >
                <MessageCircle className="w-5 h-5" />
                <span>{commentCount}</span>
              </button>
            </div>

            <button
              type="button"
              onClick={handleShare}
              className="p-1.5 rounded-lg hover:bg-[#F5F6F5] text-[#555555] hover:text-[#111111] transition-colors relative"
              aria-label="Share post"
            >
              {copied ? (
                <span className="flex items-center gap-1 text-xs text-[#3F8F49] font-bold">
                  <Check className="w-4 h-4" /> Copied
                </span>
              ) : (
                <Share2 className="w-5 h-5" />
              )}
            </button>
          </div>

          <div className="pt-3 border-t border-[#D5D8D5]/70 space-y-2">
            {/* Star Rating Display */}
            <div className="flex items-center justify-between">
              <RatingStars rating={currentRating || 5.0} size={16} />
              {isAuthenticated && (
                <div className="flex items-center gap-0.5 text-[11px] text-[#888888]">
                  <span>Your rating:</span>
                  {[1, 2, 3, 4, 5].map((s) => (
                    <button
                      key={s}
                      type="button"
                      onClick={() => handleRate(s)}
                      className={`px-1 font-bold ${userRating === s ? 'text-amber-500' : 'text-gray-400 hover:text-amber-500'}`}
                    >
                      {s}★
                    </button>
                  ))}
                </div>
              )}
            </div>

            {/* Caption */}
            {caption && (
              <p className="text-sm text-[#111111] leading-relaxed">
                {caption}
              </p>
            )}
          </div>
        </div>

        {/* Expandable Comments Drawer */}
        {showComments && (
          <div className="mt-4 pt-3 border-t border-[#D5D8D5] space-y-3">
            <h5 className="text-xs font-bold text-[#111111]">
              Comments ({commentsList.length})
            </h5>

            {loadingComments ? (
              <div className="py-3 flex justify-center">
                <Spinner size="sm" />
              </div>
            ) : commentsList.length > 0 ? (
              <div className="space-y-2 max-h-48 overflow-y-auto pr-1">
                {commentsList.map((c) => (
                  <div key={c.id} className="text-xs bg-[#F5F6F5] rounded-xl p-2.5 flex items-start justify-between gap-2">
                    <div>
                      <span className="font-bold text-[#111111] mr-1">
                        {c.displayName || `@${c.username}`}:
                      </span>
                      <span className="text-[#333333]">{c.content}</span>
                    </div>
                    {user && (user.id === c.userId || user.username === c.username) && (
                      <button
                        type="button"
                        onClick={() => handleDeleteComment(c.id)}
                        className="text-[#888888] hover:text-red-500 p-0.5 shrink-0"
                        title="Delete comment"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    )}
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-xs text-[#888888] italic py-1">No comments yet. Be the first to comment!</p>
            )}

            {/* Add Comment Input */}
            <form onSubmit={handleAddComment} className="flex gap-2 pt-1">
              <input
                type="text"
                maxLength={1000}
                placeholder={isAuthenticated ? "Add a comment..." : "Log in to comment"}
                value={newCommentText}
                onChange={(e) => setNewCommentText(e.target.value)}
                disabled={!isAuthenticated || isSubmittingComment}
                className="flex-1 bg-[#F5F6F5] border border-[#D5D8D5] rounded-lg px-2.5 py-1.5 text-xs text-[#111111] focus:border-[#5AAE62] focus:outline-none"
              />
              <button
                type="submit"
                disabled={!isAuthenticated || !newCommentText.trim() || isSubmittingComment}
                className="bg-[#5AAE62] hover:bg-[#3F8F49] text-white px-3 py-1.5 rounded-lg text-xs font-bold transition-colors disabled:opacity-50"
              >
                {isSubmittingComment ? <Spinner size="sm" className="border-white" /> : <Send className="w-3.5 h-3.5" />}
              </button>
            </form>
          </div>
        )}
      </div>
    </article>
  );
}
