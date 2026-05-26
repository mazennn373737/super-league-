import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Super League Hub - Live Scores",
  description: "Multi-tier tournament live scores, standings, and knockout brackets",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="h-full antialiased">
      <body className="min-h-full">{children}</body>
    </html>
  );
}
