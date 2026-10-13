import { useState } from 'react';
import { X } from 'lucide-react';
import Spinner from '../common/Spinner';

export default function PantryItemRow({
  item,
  onRemove,
  className = ''
}) {
  const [isDeleting, setIsDeleting] = useState(false);

  const handleRemoveClick = async () => {
    if (!onRemove || isDeleting) return;
    setIsDeleting(true);
    try {
      await onRemove(item.id);
    } finally {
      setIsDeleting(false);
    }
  };

  const quantityLabel = item.quantity
    ? `${item.quantity}${item.unit ? ` ${item.unit}` : ''}`
    : item.unit || '';

  return (
    <div className={`flex items-center justify-between p-3.5 bg-white border border-[#D5D8D5] rounded-xl hover:border-[#5AAE62] transition-all shadow-2xs group ${className}`}>
      <div className="flex items-center gap-3">
        <div className="w-2.5 h-2.5 rounded-full bg-[#5AAE62] shrink-0" />
        <span className="text-sm md:text-base font-bold text-[#111111]">
          {item.name}
        </span>
        {quantityLabel && (
          <span className="text-xs font-semibold px-2 py-0.5 rounded-md bg-[#F5F6F5] text-[#555555]">
            {quantityLabel}
          </span>
        )}
      </div>

      <button
        type="button"
        disabled={isDeleting}
        onClick={handleRemoveClick}
        className="p-1.5 rounded-lg text-[#888888] hover:text-red-500 hover:bg-red-50 transition-colors disabled:opacity-50"
        aria-label={`Remove ${item.name} from pantry`}
      >
        {isDeleting ? (
          <Spinner size="sm" className="border-red-500" />
        ) : (
          <X className="w-4 h-4" />
        )}
      </button>
    </div>
  );
}
