import { Utensils } from 'lucide-react';
import Button from './Button';

export default function EmptyState({
  icon: Icon = Utensils,
  title = 'No items found',
  description = 'Try searching for something else or adjusting your filters.',
  actionLabel,
  onAction,
  className = ''
}) {
  return (
    <div className={`flex flex-col items-center justify-center text-center p-8 bg-[#F5F6F5]/50 border border-dashed border-[#D5D8D5] rounded-2xl ${className}`}>
      <div className="w-14 h-14 rounded-full bg-[#E8F4E9] flex items-center justify-center text-[#3F8F49] mb-4">
        <Icon className="w-7 h-7" />
      </div>
      <h3 className="text-lg font-bold text-[#111111] mb-1">{title}</h3>
      <p className="text-sm text-[#555555] max-w-sm mb-6">{description}</p>
      {actionLabel && onAction && (
        <Button onClick={onAction} size="sm">
          {actionLabel}
        </Button>
      )}
    </div>
  );
}
