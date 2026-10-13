export default function Badge({
  children,
  variant = 'green',
  className = '',
  ...props
}) {
  const variants = {
    green: 'bg-[#E8F4E9] text-[#3F8F49] border border-[#5AAE62]/30',
    gray: 'bg-[#F5F6F5] text-[#555555] border border-[#D5D8D5]',
    amber: 'bg-amber-50 text-amber-800 border border-amber-200',
    red: 'bg-red-50 text-red-700 border border-red-200'
  };

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold tracking-wide ${
        variants[variant] || variants.green
      } ${className}`}
      {...props}
    >
      {children}
    </span>
  );
}
