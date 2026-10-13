export default function Card({
  children,
  className = '',
  padding = 'p-6',
  hover = false,
  onClick,
  ...props
}) {
  return (
    <div
      onClick={onClick}
      className={`bg-white border border-[#D5D8D5] rounded-2xl shadow-xs ${padding} ${
        hover ? 'hover:border-[#5AAE62] hover:shadow-md transition-all cursor-pointer' : ''
      } ${className}`}
      {...props}
    >
      {children}
    </div>
  );
}
