export const mockRecipes = [
  {
    id: 'spicy-chicken-tacos',
    title: 'AI Generated: Spicy Chicken Tacos',
    description: 'Sizzling seasoned shredded chicken served with warm tortillas, melted cheese, and fresh pico de gallo.',
    imageUrl: 'https://images.unsplash.com/photo-1565299585323-38d6b0865b47?auto=format&fit=crop&w=800&q=80',
    source: 'AI_GENERATED',
    isAiGenerated: true,
    prepTimeMinutes: 10,
    cookTimeMinutes: 15,
    servings: 3,
    spiceLevel: 'SPICY',
    ingredients: [
      { name: 'Chicken Breast', quantity: 1, unit: 'lb' },
      { name: 'Corn Tortillas', quantity: 8, unit: 'pieces' },
      { name: 'Cheese (Cheddar/Jack)', quantity: 1, unit: 'cup' },
      { name: 'Chili Powder', quantity: 1, unit: 'tbsp' },
      { name: 'Bell Peppers', quantity: 0.5, unit: 'cup' },
      { name: 'Lime Juice', quantity: 1, unit: 'tbsp' },
      { name: 'Cilantro (Optional)', quantity: 2, unit: 'tbsp' }
    ],
    instructions: [
      { step: 1, text: 'Season the chicken breast evenly with chili powder, salt, and cumin.' },
      { step: 2, text: 'Heat a skillet over medium-high heat with 1 tbsp olive oil and cook chicken for 6-8 minutes per side until cooked through.' },
      { step: 3, text: 'Shred the chicken with two forks and toss with lime juice and sautéed bell peppers.' },
      { step: 4, text: 'Warm the corn tortillas on a dry griddle for 30 seconds per side.' },
      { step: 5, text: 'Assemble tacos by layering shredded chicken, cheese, and fresh garnishes. Serve warm.' }
    ],
    substitutions: {
      'Heavy Cream': 'Milk + Butter',
      'Corn Tortillas': 'Flour Tortillas or Lettuce Wraps',
      'Chili Powder': 'Paprika + Cayenne',
      'Cheddar Cheese': 'Monterey Jack or Queso Fresco'
    }
  },
  {
    id: 'chicken-rice-bowl',
    title: 'Chicken & Rice Bowl',
    description: 'A balanced and hearty grain bowl packed with grilled chicken, fluffy jasmine rice, and seasoned black beans.',
    imageUrl: 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=800&q=80',
    source: 'AI_GENERATED',
    isAiGenerated: true,
    prepTimeMinutes: 10,
    cookTimeMinutes: 20,
    servings: 2,
    spiceLevel: 'MEDIUM',
    ingredients: [
      { name: 'Chicken Breast', quantity: 1, unit: 'lb' },
      { name: 'Jasmine Rice', quantity: 1, unit: 'cup' },
      { name: 'Black Beans', quantity: 1, unit: 'can' },
      { name: 'Onions', quantity: 0.5, unit: 'medium' },
      { name: 'Olive Oil', quantity: 1, unit: 'tbsp' }
    ],
    instructions: [
      { step: 1, text: 'Cook jasmine rice according to package instructions until tender and fluffy.' },
      { step: 2, text: 'Sauté diced onions in olive oil until golden.' },
      { step: 3, text: 'Pan-sear seasoned chicken breast pieces until golden and cooked thoroughly.' },
      { step: 4, text: 'Assemble the bowl with rice base, black beans, and chicken.' }
    ]
  },
  {
    id: 'black-bean-chili',
    title: 'Black Bean Chili',
    description: 'Smoky and rich vegetarian chili simmered with roasted tomatoes, onions, and hearty black beans.',
    imageUrl: 'https://images.unsplash.com/photo-1543339308-43e59d6b73a6?auto=format&fit=crop&w=800&q=80',
    source: 'AI_GENERATED',
    isAiGenerated: true,
    prepTimeMinutes: 10,
    cookTimeMinutes: 30,
    servings: 4,
    spiceLevel: 'SPICY',
    ingredients: [
      { name: 'Black Beans', quantity: 2, unit: 'cans' },
      { name: 'Diced Tomatoes', quantity: 1, unit: 'can' },
      { name: 'Onions', quantity: 1, unit: 'large' },
      { name: 'Garlic', quantity: 3, unit: 'cloves' },
      { name: 'Chili Powder', quantity: 2, unit: 'tbsp' }
    ],
    instructions: [
      { step: 1, text: 'Sauté chopped onions and minced garlic in a Dutch oven until fragrant.' },
      { step: 2, text: 'Add drained black beans, diced tomatoes, and chili powder.' },
      { step: 3, text: 'Simmer on low heat for 25 minutes until thick and aromatic.' }
    ]
  },
  {
    id: 'creamy-tomato-pasta',
    title: 'Creamy Tomato Pasta',
    description: 'Silky pasta tossed in a vibrant sun-ripened tomato sauce with garlic and fresh basil.',
    imageUrl: 'https://images.unsplash.com/photo-1621996346565-e3d5d6281545?auto=format&fit=crop&w=800&q=80',
    source: 'AI_GENERATED',
    isAiGenerated: true,
    prepTimeMinutes: 10,
    cookTimeMinutes: 15,
    servings: 2,
    spiceLevel: 'MILD',
    ingredients: [
      { name: 'Pasta (Penne/Fettuccine)', quantity: 250, unit: 'g' },
      { name: 'Crushed Tomatoes', quantity: 1, unit: 'cup' },
      { name: 'Heavy Cream', quantity: 0.5, unit: 'cup' },
      { name: 'Garlic', quantity: 2, unit: 'cloves' }
    ],
    instructions: [
      { step: 1, text: 'Boil pasta in salted water until al dente.' },
      { step: 2, text: 'Simmer crushed tomatoes with minced garlic in a saucepan.' },
      { step: 3, text: 'Stir in cream and toss pasta with the sauce.' }
    ]
  },
  {
    id: 'chicken-sotatois',
    title: 'Chicken & Sotatois',
    description: 'Crispy pan-roasted seasoned chicken pieces served with skillet potatoes and fresh herbs.',
    imageUrl: 'https://images.unsplash.com/photo-1532550907401-a500c9a57435?auto=format&fit=crop&w=800&q=80',
    source: 'AI_GENERATED',
    isAiGenerated: true,
    prepTimeMinutes: 10,
    cookTimeMinutes: 20,
    servings: 2,
    spiceLevel: 'MEDIUM',
    ingredients: [
      { name: 'Chicken Breast', quantity: 1, unit: 'lb' },
      { name: 'Potatoes', quantity: 3, unit: 'medium' },
      { name: 'Olive Oil', quantity: 2, unit: 'tbsp' },
      { name: 'Herbs & Seasoning', quantity: 1, unit: 'tbsp' }
    ],
    instructions: [
      { step: 1, text: 'Cube the potatoes and parboil for 5 minutes.' },
      { step: 2, text: 'Pan fry chicken and potatoes together in hot olive oil until golden and crispy.' }
    ]
  }
];
