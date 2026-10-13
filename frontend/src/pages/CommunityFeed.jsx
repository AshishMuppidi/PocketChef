import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { PlusCircle, Users, AlertCircle, RefreshCw } from 'lucide-react';
import apiClient from '../api/apiClient';
import { getApiErrorMessage } from '../utils/apiError';
import CommunityPostCard from '../components/community/CommunityPostCard';
import Button from '../components/common/Button';
import EmptyState from '../components/common/EmptyState';

export default function CommunityFeed() {
  const [posts, setPosts] = useState([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState(null);

  const fetchPosts = async (page = 0) => {
    setIsLoading(true);
    setErrorMessage(null);

    try {
      const response = await apiClient.get('/community/posts', {
        params: { page, size: 12 }
      });
      const data = response.data;
      setPosts(data.content || []);
      setCurrentPage(data.page || 0);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
    } catch (err) {
      setErrorMessage(getApiErrorMessage(err, 'Failed to load community feed.'));
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchPosts(0);
  }, []);

  const handlePageChange = (newPage) => {
    if (newPage < 0 || newPage >= totalPages) return;
    fetchPosts(newPage);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <div className="space-y-8 pb-12">
      {/* Header with Title & Create Post CTA */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 pb-4 border-b border-[#D5D8D5]">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <div className="w-8 h-8 rounded-full bg-[#E8F4E9] flex items-center justify-center text-[#3F8F49]">
              <Users className="w-4 h-4" />
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-[#111111] tracking-tight">
              Community Feed
            </h1>
          </div>
          <p className="text-sm text-[#555555]">
            Discover real recipe remakes, reviews, and culinary creations from our cooking community.
          </p>
        </div>

        <Link
          to="/community/create"
          className="inline-flex items-center gap-2 bg-[#5AAE62] hover:bg-[#3F8F49] text-white px-5 py-2.5 rounded-xl text-sm font-bold shadow-xs transition-colors shrink-0"
        >
          <PlusCircle className="w-4 h-4" />
          <span>Post a Remake</span>
        </Link>
      </div>

      {/* Error Alert */}
      {errorMessage && (
        <div className="max-w-2xl mx-auto p-4 bg-red-50 border border-red-200 rounded-2xl flex items-center justify-between gap-3 text-sm text-red-700">
          <div className="flex items-center gap-2">
            <AlertCircle className="w-5 h-5 shrink-0" />
            <span>{errorMessage}</span>
          </div>
          <Button
            size="sm"
            variant="secondary"
            icon={RefreshCw}
            onClick={() => fetchPosts(currentPage)}
          >
            Retry
          </Button>
        </div>
      )}

      {/* 3-Column Grid on Desktop, 2 on Tablet, 1 on Mobile */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3, 4, 5, 6].map((idx) => (
            <div
              key={idx}
              className="bg-white border border-[#D5D8D5] rounded-2xl overflow-hidden shadow-xs animate-pulse p-4 space-y-4"
            >
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-gray-200" />
                <div className="space-y-1.5 flex-1">
                  <div className="h-4 bg-gray-200 rounded w-1/3" />
                  <div className="h-3 bg-gray-200 rounded w-1/4" />
                </div>
              </div>
              <div className="w-full aspect-4/3 bg-gray-200 rounded-xl" />
              <div className="h-4 bg-gray-200 rounded w-3/4" />
            </div>
          ))}
        </div>
      ) : posts.length > 0 ? (
        <div className="space-y-8">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {posts.map((post) => (
              <CommunityPostCard
                key={post.id}
                id={post.id}
                username={post.username}
                displayName={post.displayName}
                avatar={post.profileImageUrl}
                image={post.imageUrl}
                recipeId={post.recipeId}
                recipeTitle={post.recipeTitle}
                likes={post.likeCount}
                comments={post.commentCount}
                rating={post.averageRating}
                caption={post.caption}
                isLikedByCurrentUser={post.isLikedByCurrentUser}
                currentUserRating={post.currentUserRating}
              />
            ))}
          </div>

          {/* Pagination Controls */}
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-3 pt-6 border-t border-[#D5D8D5]">
              <Button
                variant="secondary"
                size="sm"
                disabled={currentPage === 0}
                onClick={() => handlePageChange(currentPage - 1)}
              >
                Previous
              </Button>

              <span className="text-xs font-bold text-[#555555] px-2">
                Page {currentPage + 1} of {totalPages}
              </span>

              <Button
                variant="secondary"
                size="sm"
                disabled={currentPage >= totalPages - 1}
                onClick={() => handlePageChange(currentPage + 1)}
              >
                Next
              </Button>
            </div>
          )}
        </div>
      ) : (
        <div className="max-w-2xl mx-auto py-12">
          <EmptyState
            title="No community posts yet"
            description="Be the first chef to cook a recipe, take a photo, and share your remake with the community!"
            actionLabel="Post a Remake"
            onAction={() => (window.location.href = '/community/create')}
          />
        </div>
      )}
    </div>
  );
}
