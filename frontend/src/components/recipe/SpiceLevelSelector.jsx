import { Flame } from 'lucide-react';
import Spinner from '../common/Spinner';

export default function SpiceLevelSelector({
  value = 'MEDIUM',
  onChange,
  isLoading = false,
  className = ''
}) {
  const levels = [
    { key: 'MILD', label: 'Mild' },
    { key: 'MEDIUM', label: 'Medium' },
    { key: 'SPICY', label: 'Spicy' }
  ];

  return (
    <div className={`bg-white border border-[#D5D8D5] rounded-2xl p-6 shadow-xs ${className}`}>
      <div className="flex items-center justify-between gap-2 mb-4">
        <div className="flex items-center gap-2">
          <Flame className="w-5 h-5 text-[#5AAE62]" />
          <h3 className="text-lg font-bold text-[#111111]">Spice Level</h3>
        </div>
        {isLoading && (
          <span className="flex items-center gap-1.5 text-xs text-[#5AAE62] font-semibold">
            <Spinner size="sm" />
            Adjusting...
          </span>
        )}
      </div>

      {/* Green segmented control container */}
      <div className="bg-[#5AAE62] p-1.5 rounded-xl flex items-center justify-between gap-1 shadow-inner">
        {levels.map((level) => {
          const isSelected = value.toUpperCase() === level.key;
          return (
            <button
              key={level.key}
              type="button"
              disabled={isLoading}
              onClick={() => onChange && onChange(level.key)}
              className={`flex-1 py-2 px-3 rounded-lg text-sm font-bold transition-all disabled:opacity-75 ${
                isSelected
                  ? 'bg-white text-[#111111] shadow-xs scale-100'
                  : 'text-white/90 hover:text-white hover:bg-[#3F8F49]/40'
              }`}
            >
              {level.label}
            </button>
          );
        })}
      </div>
    </div>
  );
}
