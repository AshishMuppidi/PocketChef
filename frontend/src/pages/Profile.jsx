import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { User, Bookmark, MessageSquare, LogOut, Calendar } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { mockRecipes } from '../data/mockRecipes';
import { mockCommunityPosts } from '../data/mockCommunityPosts';
import RecipeCard from '../components/recipe/RecipeCard';
import CommunityPostCard from '../components/community/CommunityPostCard';
import Card from '../components/common/Card';
import Button from '../components/common/Button';

export default function Profile() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('SAVED');

  const handleLogout = () => {
    logout();
    navigate('/', { replace: true });
  };

  const displayName = user?.displayName || user?.username || 'Culinary Explorer';
  const username = user?.username ? `@${user.username}` : '@user';
  const email = user?.email || 'user@whatsonmyplate.local';
  const initialLetter = displayName.charAt(0).toUpperCase();

  const formattedDate = user?.createdAt
    ? new Date(user.createdAt).toLocaleDateString('en-US', { month: 'short', year: 'numeric' })
    : 'Recently';

  const savedRecipes = mockRecipes.slice(0, 2);
  const userPosts = mockCommunityPosts.slice(0, 2);

  return (
    <div className="max-w-4xl mx-auto space-y-8 pb-12">
      {/* Profile Header Card */}
      <Card className="p-6 sm:p-8 flex flex-col sm:flex-row items-center justify-between gap-6">
        <div className="flex flex-col sm:flex-row items-center gap-6 text-center sm:text-left">
          <div className="w-20 h-20 rounded-full bg-[#E8F4E9] text-[#3F8F49] flex items-center justify-center text-3xl font-extrabold border-2 border-[#5AAE62] shrink-0">
            {initialLetter}
          </div>
          <div className="space-y-1">
            <h1 className="text-2xl font-extrabold text-[#111111]">
              {displayName}
            </h1>
            <p className="text-sm text-[#555555]">
              {username} • {email}
            </p>
            <div className="flex flex-wrap items-center justify-center sm:justify-start gap-2 pt-1">
              <span className="inline-block text-xs font-semibold px-3 py-0.5 bg-[#E8F4E9] text-[#3F8F49] rounded-full border border-[#5AAE62]/30">
                Home Chef
              </span>
              <span className="inline-flex items-center gap-1 text-xs text-[#888888]">
                <Calendar className="w-3 h-3" />
                Joined {formattedDate}
              </span>
            </div>
          </div>
        </div>

        <Button
          variant="secondary"
          size="sm"
          icon={LogOut}
          onClick={handleLogout}
          className="shrink-0 text-red-600 hover:bg-red-50 hover:border-red-200"
        >
          Sign Out
        </Button>
      </Card>

      {/* Tabs */}
      <div className="flex border-b border-[#D5D8D5] gap-8">
        <button
          type="button"
          onClick={() => setActiveTab('SAVED')}
          className={`flex items-center gap-2 pb-3 font-bold text-sm border-b-2 transition-all ${
            activeTab === 'SAVED'
              ? 'border-[#5AAE62] text-[#3F8F49]'
              : 'border-transparent text-[#555555] hover:text-[#111111]'
          }`}
        >
          <Bookmark className="w-4 h-4" />
          <span>Saved Recipes ({savedRecipes.length})</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('POSTS')}
          className={`flex items-center gap-2 pb-3 font-bold text-sm border-b-2 transition-all ${
            activeTab === 'POSTS'
              ? 'border-[#5AAE62] text-[#3F8F49]'
              : 'border-transparent text-[#555555] hover:text-[#111111]'
          }`}
        >
          <MessageSquare className="w-4 h-4" />
          <span>My Remake Posts ({userPosts.length})</span>
        </button>
      </div>

      {/* Tab Contents */}
      {activeTab === 'SAVED' ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
          {savedRecipes.map((recipe) => (
            <RecipeCard
              key={recipe.id}
              id={recipe.id}
              title={recipe.title}
              description={recipe.description}
              imageUrl={recipe.imageUrl}
              prepTimeMinutes={recipe.prepTimeMinutes}
              cookTimeMinutes={recipe.cookTimeMinutes}
              isAiGenerated={recipe.isAiGenerated}
            />
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {userPosts.map((post) => (
            <CommunityPostCard
              key={post.id}
              username={post.username}
              avatar={post.avatar}
              image={post.image}
              likes={post.likes}
              comments={post.comments}
              rating={post.rating}
              caption={post.caption}
            />
          ))}
        </div>
      )}
    </div>
  );
}
